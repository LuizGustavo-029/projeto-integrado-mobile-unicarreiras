package com.estagiopro.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.estagiopro.app.data.Candidatura;
import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

import java.util.Calendar;
import java.util.List;

/**
 * Perfil de um candidato. Dois modos:
 * - APP_ID: empresa vendo uma candidatura (favoritar, analisar, entrevistar, contratar, recusar);
 * - STUDENT_ID: instituição vendo um aluno (encaminhar para vaga, alterar situação).
 */
public class CandidatoDetalheActivity extends BaseActivity {

    private long appId;
    private long studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidato_detalhe);
        Ui.statusBar(this, R.color.bg, true);
        appId = getIntent().getLongExtra(Ui.APP_ID, 0);
        studentId = getIntent().getLongExtra(Ui.STUDENT_ID, 0);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    @Override
    protected void bind() {
        final Candidatura app = appId != 0 ? Repo.getApp(this, appId) : null;
        final User s = app != null ? app.student : Repo.getUser(this, studentId);
        if (s == null || (appId != 0 && app == null)) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.tvAvatar)).setText(Fmt.initialsList(s.name));
        ((TextView) findViewById(R.id.tvNome)).setText(s.name);
        ((TextView) findViewById(R.id.tvSub)).setText(s.shortLine());
        TextView match = findViewById(R.id.tvMatch);
        if (app != null) {
            match.setVisibility(View.VISIBLE);
            match.setText(app.match + "% match");
        } else {
            match.setVisibility(View.VISIBLE);
            match.setText(s.status);
        }

        bindContact((TextView) findViewById(R.id.tvEmail), s.email, () -> Ui.mail(this, s.email));
        bindContact((TextView) findViewById(R.id.tvTelefone), s.phone, () -> Ui.dial(this, s.phone));
        bindContact((TextView) findViewById(R.id.tvLinkedin), s.linkedin, () -> Ui.openUrl(this, s.linkedin));
        ((TextView) findViewById(R.id.tvSobre)).setText(s.about.trim().isEmpty() ? "Sem descrição." : s.about);
        List<String> skills = s.skillList();
        if (skills.isEmpty()) skills = java.util.Collections.singletonList("Nenhuma competência informada");
        Ui.addChips((ViewGroup) findViewById(R.id.flSkills), skills, R.layout.item_skill);

        TextView cvName = findViewById(R.id.tvCvNome);
        TextView cvInfo = findViewById(R.id.tvCvInfo);
        if (s.hasCv()) {
            cvName.setText(s.cvName);
            String size = Fmt.fileSize(s.cvSize);
            cvInfo.setText(size.isEmpty() ? "Toque para abrir" : size + " · toque para abrir");
        } else {
            cvName.setText("Sem currículo");
            cvInfo.setText("O aluno ainda não anexou um arquivo");
        }
        findViewById(R.id.btnCurriculo).setOnClickListener(v -> {
            if (s.hasCv()) CvHelper.open(this, s.cvUri, s.cvName);
            else Ui.toast(this, "O aluno ainda não anexou um currículo");
        });

        boolean empresa = User.EMPRESA.equals(me.role);
        findViewById(R.id.llAcoesEmpresa).setVisibility(empresa && app != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.llAcoesInstituicao).setVisibility(!empresa ? View.VISIBLE : View.GONE);
        View card = findViewById(R.id.cardAndamento);
        card.setVisibility(app != null ? View.VISIBLE : View.GONE);

        if (app != null) {
            ((TextView) findViewById(R.id.tvEtapa)).setText(
                    (app.job != null ? app.job.title + " · " : "") + Candidatura.stageName(app.stage));
            TextView tvEnt = findViewById(R.id.tvEntrevista);
            if (app.stage == Candidatura.ENTREVISTA && app.interviewAt > 0) {
                tvEnt.setVisibility(View.VISIBLE);
                tvEnt.setText("Entrevista: " + Fmt.interview(app.interviewAt));
            } else {
                tvEnt.setVisibility(View.GONE);
            }
        }
        if (empresa && app != null) bindEmpresa(app);
        if (!empresa) bindInstituicao(s, app);
    }

    private void bindContact(TextView tv, String value, Runnable click) {
        if (value == null || value.trim().isEmpty()) {
            tv.setText("Não informado");
            tv.setOnClickListener(null);
            tv.setClickable(false);
        } else {
            tv.setText(value);
            tv.setOnClickListener(v -> click.run());
        }
    }

    // ------------------------------------------------------------ empresa
    private void bindEmpresa(final Candidatura app) {
        View fav = findViewById(R.id.btnFavoritar);
        Ui.setLabel(fav, app.favorite ? "Remover dos favoritos" : "Favoritar candidato");
        fav.setOnClickListener(v -> {
            boolean now = Repo.toggleFavorite(this, app.id);
            Ui.toast(this, now ? "Candidato favoritado" : "Removido dos favoritos");
            bind();
        });

        boolean finished = app.stage >= Candidatura.CONTRATADO;
        findViewById(R.id.btnAnalise).setVisibility(app.stage == Candidatura.ENVIADA ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnEntrevista).setVisibility(!finished ? View.VISIBLE : View.GONE);
        Ui.setLabel(findViewById(R.id.btnEntrevista),
                app.stage == Candidatura.ENTREVISTA ? "Reagendar entrevista" : "Agendar entrevista");
        findViewById(R.id.btnContratar).setVisibility(!finished ? View.VISIBLE : View.GONE);
        View recusar = findViewById(R.id.btnRecusar);
        recusar.setVisibility(app.stage != Candidatura.RECUSADO ? View.VISIBLE : View.GONE);
        if (finished) {
            // permite reabrir o processo
            Ui.setLabel(findViewById(R.id.btnAnalise), "Reabrir processo");
            findViewById(R.id.btnAnalise).setVisibility(View.VISIBLE);
        } else {
            Ui.setLabel(findViewById(R.id.btnAnalise), "Marcar em análise");
        }

        findViewById(R.id.btnAnalise).setOnClickListener(v -> {
            Repo.setStage(this, app.id, Candidatura.ANALISE, 0);
            Ui.toast(this, "Candidatura em análise");
            bind();
        });
        findViewById(R.id.btnEntrevista).setOnClickListener(v -> pickInterview(app));
        findViewById(R.id.btnContratar).setOnClickListener(v ->
                Ui.confirm(this, "Contratar candidato",
                        "Confirmar a contratação de " + Fmt.shortName(app.student.name) + "?", "Contratar", () -> {
                            Repo.setStage(this, app.id, Candidatura.CONTRATADO, 0);
                            Ui.toast(this, "Candidato contratado!");
                            bind();
                        }));
        recusar.setOnClickListener(v ->
                Ui.confirm(this, "Recusar candidato",
                        "O aluno será avisado de que o processo foi encerrado.", "Recusar", () -> {
                            Repo.setStage(this, app.id, Candidatura.RECUSADO, 0);
                            Ui.toast(this, "Candidatura recusada");
                            bind();
                        }));
    }

    private void pickInterview(final Candidatura app) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        new DatePickerDialog(this, (dp, y, m, d) -> {
            cal.set(y, m, d);
            new TimePickerDialog(this, (tp, h, min) -> {
                cal.set(Calendar.HOUR_OF_DAY, h);
                cal.set(Calendar.MINUTE, min);
                cal.set(Calendar.SECOND, 0);
                Repo.setStage(this, app.id, Candidatura.ENTREVISTA, cal.getTimeInMillis());
                Ui.toast(this, "Entrevista agendada para " + Fmt.interview(cal.getTimeInMillis()).toLowerCase());
                bind();
            }, 14, 0, true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ------------------------------------------------------------ instituição
    private void bindInstituicao(final User s, Candidatura app) {
        findViewById(R.id.btnEncaminhar).setOnClickListener(v -> {
            final List<Job> jobs = Repo.searchJobs(this, null, "", null, false, false, false);
            final List<Job> open = new java.util.ArrayList<>();
            for (Job j : jobs) {
                if (Job.ATIVA.equals(j.status) && Repo.appFor(this, s.id, j.id) == null) open.add(j);
            }
            if (open.isEmpty()) {
                Ui.toast(this, "Não há vagas abertas disponíveis para este aluno");
                return;
            }
            String[] names = new String[open.size()];
            for (int i = 0; i < names.length; i++) {
                names[i] = open.get(i).title + " · " + Fmt.shortCompany(open.get(i).companyName);
            }
            Ui.choose(this, "Encaminhar para qual vaga?", names, which -> {
                long id = Repo.refer(this, s.id, open.get(which).id, me.orgName);
                Ui.toast(this, id > 0 ? "Aluno encaminhado" : "Aluno já está nesta vaga");
                bind();
            });
        });
        findViewById(R.id.btnSituacao).setOnClickListener(v ->
                Ui.choose(this, "Situação do aluno",
                        new String[]{User.ST_DISP, User.ST_ENC, User.ST_CONT}, which -> {
                            String[] st = {User.ST_DISP, User.ST_ENC, User.ST_CONT};
                            Repo.setStudentStatus(this, s.id, st[which]);
                            Ui.toast(this, "Situação: " + st[which]);
                            bind();
                        }));
    }
}
