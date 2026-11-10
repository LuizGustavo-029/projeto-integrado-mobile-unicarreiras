package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

/** Criar ou editar uma vaga (empresa) / oportunidade (instituição). */
public class PublicarVagaActivity extends BaseActivity {

    private static final String[] MODES = {"Remoto", "Híbrido", "Presencial"};

    private Ui.Group modes;
    private Job editing;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicar_vaga);
        Ui.statusBar(this, R.color.bg, true);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        modes = Ui.options(this, 1, null, R.id.optRemoto, R.id.optHibrido, R.id.optPresencial);
    }

    @Override
    protected void bind() {
        if (loaded) return;
        loaded = true;
        final boolean inst = User.INSTITUICAO.equals(me.role);
        long id = getIntent().getLongExtra(Ui.JOB_ID, 0);
        if (id != 0) {
            editing = Repo.getJob(this, id);
            if (editing != null && editing.ownerId != me.id) editing = null;
        }
        findViewById(R.id.llEmpresa).setVisibility(inst ? View.VISIBLE : View.GONE);

        View btnPublicar = findViewById(R.id.btnPublicar);
        View btnRascunho = findViewById(R.id.btnRascunho);
        final boolean draftLike = editing == null || Job.RASCUNHO.equals(editing.status) || Job.REVISAO.equals(editing.status);

        setTitleText(editing == null ? (inst ? "Nova oportunidade" : "Publicar vaga") : "Editar vaga");
        Ui.setLabel(btnPublicar, editing == null ? (inst ? "Publicar oportunidade" : "Publicar vaga")
                : (draftLike ? "Publicar vaga" : "Salvar alterações"));
        Ui.setLabel(btnRascunho, inst ? "Salvar para revisão" : "Salvar como rascunho");
        btnRascunho.setVisibility(draftLike ? View.VISIBLE : View.GONE);

        if (editing != null) {
            ((EditText) findViewById(R.id.etTitulo)).setText(editing.title);
            ((EditText) findViewById(R.id.etEmpresa)).setText(editing.companyName);
            ((EditText) findViewById(R.id.etArea)).setText(editing.area);
            ((EditText) findViewById(R.id.etLocal)).setText(editing.location);
            ((EditText) findViewById(R.id.etBolsa)).setText(editing.salary);
            ((EditText) findViewById(R.id.etCarga)).setText(editing.hours);
            ((EditText) findViewById(R.id.etDescricao)).setText(editing.description);
            ((EditText) findViewById(R.id.etRequisitos)).setText(editing.requirements);
            for (int i = 0; i < MODES.length; i++) {
                if (MODES[i].equals(editing.mode)) modes.select(i, false);
            }
        }
        btnPublicar.setOnClickListener(v -> save(true));
        btnRascunho.setOnClickListener(v -> save(false));
    }

    private void setTitleText(String t) {
        ((android.widget.TextView) findViewById(R.id.tvTituloTela)).setText(t);
    }

    private void save(boolean publish) {
        boolean inst = User.INSTITUICAO.equals(me.role);
        EditText etTitulo = findViewById(R.id.etTitulo);
        EditText etEmpresa = findViewById(R.id.etEmpresa);
        String titulo = Ui.text(etTitulo);
        if (titulo.length() < 3) {
            etTitulo.setError("Informe o título da vaga");
            etTitulo.requestFocus();
            return;
        }
        String empresa = inst ? Ui.text(etEmpresa) : me.orgName;
        if (inst && empresa.length() < 2) {
            etEmpresa.setError("Informe a empresa parceira");
            etEmpresa.requestFocus();
            return;
        }
        EditText etDesc = findViewById(R.id.etDescricao);
        if (publish && Ui.text(etDesc).length() < 10) {
            etDesc.setError("Descreva as atividades da vaga");
            etDesc.requestFocus();
            return;
        }

        Job j = editing != null ? editing : new Job();
        j.ownerId = me.id;
        j.source = inst ? Job.SRC_INSTITUICAO : Job.SRC_EMPRESA;
        j.companyName = empresa;
        j.title = titulo;
        j.area = Ui.text((EditText) findViewById(R.id.etArea));
        j.location = Ui.text((EditText) findViewById(R.id.etLocal));
        j.mode = MODES[modes.selected()];
        j.salary = Fmt.salary(Ui.text((EditText) findViewById(R.id.etBolsa)));
        j.hours = Ui.text((EditText) findViewById(R.id.etCarga));
        j.description = Ui.text(etDesc);
        j.requirements = Ui.text((EditText) findViewById(R.id.etRequisitos));

        boolean wasLive = editing != null && (Job.ATIVA.equals(j.status) || Job.PAUSADA.equals(j.status)
                || Job.ENCERRADA.equals(j.status) || Job.EXPIRADA.equals(j.status));
        if (!wasLive) {
            j.status = publish ? Job.ATIVA : (inst ? Job.REVISAO : Job.RASCUNHO);
        }
        Repo.saveJob(this, j);
        Ui.hideKeyboard(this);
        Ui.toast(this, wasLive ? "Alterações salvas"
                : (publish ? "Vaga publicada!" : (inst ? "Enviada para revisão" : "Rascunho salvo")));
        finish();
    }
}
