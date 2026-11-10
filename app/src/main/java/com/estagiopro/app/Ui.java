package com.estagiopro.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;

import java.util.List;

/** Funções de apoio compartilhadas por todas as telas. */
public final class Ui {

    private Ui() { }

    public static final String JOB_ID = "job_id";
    public static final String APP_ID = "app_id";
    public static final String STUDENT_ID = "student_id";

    public static final int GREEN = 0;
    public static final int ORANGE = 1;
    public static final int BLUE = 2;
    public static final int GRAY = 3;

    public interface IntCb { void on(int value); }

    public interface StrCb { void on(String value); }

    /** Destinos da barra de navegação inferior de cada perfil. */
    public static final Class<?>[] NAV_ALUNO = {
            HomeAlunoActivity.class, BuscarVagasActivity.class,
            CandidaturasActivity.class, PerfilAlunoActivity.class};
    public static final Class<?>[] NAV_EMPRESA = {
            PainelEmpresaActivity.class, VagasEmpresaActivity.class,
            CandidatosEmpresaActivity.class, PerfilEmpresaActivity.class};
    public static final Class<?>[] NAV_INSTITUICAO = {
            PainelInstituicaoActivity.class, VagasInstituicaoActivity.class,
            AlunosEgressosActivity.class, PerfilInstituicaoActivity.class};

    private static final int[] NAV_ITEM = {R.id.navItem0, R.id.navItem1, R.id.navItem2, R.id.navItem3};
    private static final int[] NAV_ICON = {R.id.navIcon0, R.id.navIcon1, R.id.navIcon2, R.id.navIcon3};
    private static final int[] NAV_LABEL = {R.id.navLabel0, R.id.navLabel1, R.id.navLabel2, R.id.navLabel3};

    // ------------------------------------------------------------------ barra de status
    /** @param darkIcons true = ícones escuros (para fundo claro). */
    public static void statusBar(Activity a, int colorRes, boolean darkIcons) {
        Window w = a.getWindow();
        w.setStatusBarColor(ContextCompat.getColor(a, colorRes));
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(w, w.getDecorView());
        c.setAppearanceLightStatusBars(darkIcons);
        c.setAppearanceLightNavigationBars(true);
    }

    // ------------------------------------------------------------------ navegação
    public static Class<?> homeFor(User u) {
        if (User.EMPRESA.equals(u.role)) return PainelEmpresaActivity.class;
        if (User.INSTITUICAO.equals(u.role)) return PainelInstituicaoActivity.class;
        return HomeAlunoActivity.class;
    }

    public static void goHome(Activity a, User u) {
        goRoot(a, homeFor(u));
    }

    public static void go(Activity from, Class<?> to) {
        from.startActivity(new Intent(from, to));
    }

    public static void go(Activity from, Class<?> to, String extra, long value) {
        Intent i = new Intent(from, to);
        i.putExtra(extra, value);
        from.startActivity(i);
    }

    /** Troca de aba sem animação e sem empilhar telas. */
    @SuppressWarnings("deprecation")
    public static void switchTo(Activity from, Class<?> to) {
        Intent i = new Intent(from, to);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        from.startActivity(i);
        from.finish();
        from.overridePendingTransition(0, 0);
    }

    public static void goAndFinish(Activity from, Class<?> to) {
        from.startActivity(new Intent(from, to));
        from.finish();
    }

    /** Abre a tela limpando o histórico (login, logout, fim do cadastro). */
    public static void goRoot(Activity from, Class<?> to) {
        Intent i = new Intent(from, to);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(i);
        from.finish();
    }

    /** Configura a barra inferior: destaca a aba ativa e abre as demais. */
    public static void setupNav(final Activity a, final Class<?>[] targets, int active) {
        int blue = ContextCompat.getColor(a, R.color.blue);
        for (int i = 0; i < NAV_ITEM.length; i++) {
            if (i == active) {
                ((ImageView) a.findViewById(NAV_ICON[i])).setColorFilter(blue);
                ((TextView) a.findViewById(NAV_LABEL[i])).setTextColor(blue);
                continue;
            }
            final Class<?> target = targets[i];
            a.findViewById(NAV_ITEM[i]).setOnClickListener(v -> switchTo(a, target));
        }
    }

    /** Sino de notificações com o ponto vermelho quando há não lidas. */
    public static void bell(final Activity a, long userId) {
        View dot = a.findViewById(R.id.viewDot);
        if (dot != null) dot.setVisibility(Repo.unread(a, userId) > 0 ? View.VISIBLE : View.GONE);
        View bell = a.findViewById(R.id.btnNotificacoes);
        if (bell != null) bell.setOnClickListener(v -> go(a, NotificacoesActivity.class));
    }

    // ------------------------------------------------------------------ mensagens e teclado
    public static void toast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Activity a) {
        View v = a.getCurrentFocus();
        if (v == null) return;
        InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static String text(EditText et) {
        return et.getText().toString().trim();
    }

    // ------------------------------------------------------------------ senha
    /** Mostra / oculta a senha ao tocar no olho. */
    public static void passwordToggle(final Activity a, int editId, int eyeId) {
        final EditText et = a.findViewById(editId);
        final ImageView eye = a.findViewById(eyeId);
        eye.setOnClickListener(new View.OnClickListener() {
            private boolean visible = false;

            @Override
            public void onClick(View v) {
                visible = !visible;
                int start = et.getSelectionStart();
                int end = et.getSelectionEnd();
                et.setInputType(InputType.TYPE_CLASS_TEXT
                        | (visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                   : InputType.TYPE_TEXT_VARIATION_PASSWORD));
                if (start >= 0 && end >= 0) et.setSelection(start, end);
                eye.setImageResource(visible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
            }
        });
    }

    // ------------------------------------------------------------------ termos de uso
    private static final String TERMOS = "1. O EstágioPro conecta estudantes, empresas e instituições de ensino para "
            + "divulgação e acompanhamento de vagas de estágio.\n\n"
            + "2. Você é responsável pela veracidade das informações do seu perfil e das vagas que publicar.\n\n"
            + "3. É proibido publicar conteúdo falso, discriminatório ou que cobre valores de candidatos.\n\n"
            + "4. Contas que violarem estes termos podem ser suspensas.";
    private static final String PRIVACIDADE = "Coletamos apenas os dados necessários para o funcionamento do app: "
            + "nome, e-mail, informações acadêmicas e profissionais e o currículo que você anexar.\n\n"
            + "Seus dados ficam armazenados neste dispositivo e só são exibidos a empresas e instituições "
            + "quando você se candidata ou é encaminhado a uma vaga.\n\n"
            + "Você pode editar ou excluir seus dados a qualquer momento no seu perfil.";

    private static void dialogText(Context c, String title, String body) {
        new AlertDialog.Builder(c).setTitle(title).setMessage(body).setPositiveButton("Entendi", null).show();
    }

    /** Deixa "Termos de Uso" e "Política de Privacidade" em azul, em negrito e clicáveis. */
    public static void highlightTerms(final Activity a, TextView tv) {
        String full = tv.getText().toString();
        SpannableString s = new SpannableString(full);
        link(a, s, full, "Termos de Uso", "Termos de Uso", TERMOS);
        link(a, s, full, "Política de Privacidade", "Política de Privacidade", PRIVACIDADE);
        tv.setText(s);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setHighlightColor(Color.TRANSPARENT);
    }

    private static void link(final Activity a, SpannableString s, String full, String part, final String title, final String body) {
        int i = full.indexOf(part);
        if (i < 0) return;
        s.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                dialogText(a, title, body);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ContextCompat.getColor(a, R.color.blue));
                ds.setFakeBoldText(true);
                ds.setUnderlineText(false);
            }
        }, i, i + part.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // ------------------------------------------------------------------ views auxiliares
    /** Primeiro TextView filho do botão (os botões do layout não têm id no texto). */
    public static TextView label(View container) {
        if (container instanceof TextView) return (TextView) container;
        if (container instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) container;
            for (int i = 0; i < g.getChildCount(); i++) {
                View ch = g.getChildAt(i);
                if (ch instanceof TextView) return (TextView) ch;
            }
        }
        return null;
    }

    public static void setLabel(View container, String text) {
        TextView t = label(container);
        if (t != null) t.setText(text);
    }

    /** Ícone (primeiro ImageView) de um botão. */
    public static ImageView iconOf(View container) {
        if (container instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) container;
            for (int i = 0; i < g.getChildCount(); i++) {
                View ch = g.getChildAt(i);
                if (ch instanceof ImageView) return (ImageView) ch;
            }
        }
        return null;
    }

    public static void setBadge(TextView tv, String text, int kind) {
        tv.setText(text);
        Context c = tv.getContext();
        int bg;
        int color;
        switch (kind) {
            case ORANGE:
                bg = R.drawable.bg_badge_orange;
                color = R.color.orange_text;
                break;
            case BLUE:
                bg = R.drawable.bg_badge_blue;
                color = R.color.blue;
                break;
            case GRAY:
                bg = R.drawable.bg_chip_dark;
                color = R.color.chip_dark_text;
                break;
            default:
                bg = R.drawable.bg_badge_green;
                color = R.color.green_text;
                break;
        }
        tv.setBackgroundResource(bg);
        tv.setTextColor(ContextCompat.getColor(c, color));
    }

    public static View inflate(Context c, int layout, ViewGroup parent) {
        return LayoutInflater.from(c).inflate(layout, parent, false);
    }

    /** Preenche um container com chips (item_chip, item_chip_dark ou item_skill). */
    public static void addChips(ViewGroup parent, List<String> items, int layoutRes) {
        parent.removeAllViews();
        for (String s : items) {
            TextView t = (TextView) inflate(parent.getContext(), layoutRes, parent);
            t.setText(s);
            parent.addView(t);
        }
    }

    public static void empty(LinearLayout parent, String message) {
        TextView t = (TextView) inflate(parent.getContext(), R.layout.item_empty, parent);
        t.setText(message);
        parent.addView(t);
    }

    /** Botão "Mostrar mais" no fim de uma lista. */
    public static void more(LinearLayout parent, final Runnable onClick) {
        TextView t = (TextView) inflate(parent.getContext(), R.layout.item_more, parent);
        t.setOnClickListener(v -> onClick.run());
        parent.addView(t);
    }

    public static void groupLabel(LinearLayout parent, String text) {
        TextView t = (TextView) inflate(parent.getContext(), R.layout.item_group_label, parent);
        t.setText(text);
        parent.addView(t);
    }

    /** Destaca (ou não) o cartão de estatística que contém o número. */
    public static void statSelect(TextView number, boolean selected) {
        Context c = number.getContext();
        View card = (View) number.getParent();
        card.setBackgroundResource(selected ? R.drawable.bg_card_selected : R.drawable.bg_card);
        number.setTextColor(ContextCompat.getColor(c, selected ? R.color.blue : R.color.ink));
    }

    // ------------------------------------------------------------------ grupos de seleção
    public interface SelectListener { void onSelect(int index); }

    /** Abas segmentadas, chips de filtro ou opções: só um item selecionado. */
    public static final class Group {
        public static final int TAB = 0;
        public static final int CHIP = 1;
        public static final int OPTION = 2;

        private final Activity a;
        private final int[] ids;
        private final int type;
        private final SelectListener listener;
        private int selected;

        Group(Activity a, int type, int selected, SelectListener l, int[] ids) {
            this.a = a;
            this.type = type;
            this.selected = selected;
            this.listener = l;
            this.ids = ids;
            for (int i = 0; i < ids.length; i++) {
                final int index = i;
                a.findViewById(ids[i]).setOnClickListener(v -> select(index, true));
            }
            apply();
        }

        public int selected() {
            return selected;
        }

        public void select(int index, boolean notify) {
            selected = index;
            apply();
            if (notify && listener != null) listener.onSelect(index);
        }

        private void apply() {
            for (int i = 0; i < ids.length; i++) {
                boolean sel = (i == selected);
                View v = a.findViewById(ids[i]);
                switch (type) {
                    case TAB:
                        TextView t = (TextView) v;
                        t.setBackground(sel ? ContextCompat.getDrawable(a, R.drawable.bg_tab_selected) : null);
                        t.setTextColor(ContextCompat.getColor(a, sel ? R.color.ink : R.color.muted));
                        break;
                    case CHIP:
                        TextView c = (TextView) v;
                        c.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_outline);
                        c.setTextColor(ContextCompat.getColor(a, sel ? R.color.white : R.color.chip_dark_text));
                        break;
                    default:
                        applyOption((LinearLayout) v, sel);
                        break;
                }
            }
        }

        private void applyOption(LinearLayout box, boolean sel) {
            box.setBackgroundResource(sel ? R.drawable.bg_option_selected : R.drawable.bg_btn_outline);
            ImageView icon = (ImageView) box.getChildAt(0);
            icon.setColorFilter(ContextCompat.getColor(a, sel ? R.color.navy : R.color.muted));
            TextView label = (TextView) box.getChildAt(1);
            label.setTextColor(ContextCompat.getColor(a, sel ? R.color.navy : R.color.chip_text));
            View last = box.getChildAt(box.getChildCount() - 1);
            ImageView check;
            if (box.getChildCount() >= 3 && last instanceof ImageView) {
                check = (ImageView) last;
            } else {
                check = new ImageView(a);
                check.setImageResource(R.drawable.ic_check);
                check.setColorFilter(ContextCompat.getColor(a, R.color.blue));
                box.addView(check, new LinearLayout.LayoutParams(dp(a, 16), dp(a, 16)));
            }
            check.setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    public static Group tabs(Activity a, int selected, SelectListener l, int... ids) {
        return new Group(a, Group.TAB, selected, l, ids);
    }

    public static Group chips(Activity a, int selected, SelectListener l, int... ids) {
        return new Group(a, Group.CHIP, selected, l, ids);
    }

    public static Group options(Activity a, int selected, SelectListener l, int... ids) {
        return new Group(a, Group.OPTION, selected, l, ids);
    }

    // ------------------------------------------------------------------ diálogos
    public static void confirm(Context c, String title, String msg, String ok, final Runnable onOk) {
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(ok, (d, w) -> onOk.run())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public static void choose(Context c, String title, String[] items, final IntCb cb) {
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setItems(items, (d, w) -> cb.on(w))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public static void input(final Context c, String title, String hint, String initial, final StrCb cb) {
        final EditText et = new EditText(c);
        et.setHint(hint);
        et.setText(initial);
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        FrameLayout box = new FrameLayout(c);
        int pad = dp(c, 20);
        box.setPadding(pad, dp(c, 8), pad, 0);
        box.addView(et, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setView(box)
                .setPositiveButton("Salvar", (d, w) -> cb.on(et.getText().toString().trim()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static EditText passField(Context c, LinearLayout box, String hint) {
        EditText et = new EditText(c);
        et.setHint(hint);
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(et, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return et;
    }

    /** Diálogo "Alterar senha" (senha atual + nova senha). */
    public static void changePasswordDialog(final Activity a, final long userId) {
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(a, 20);
        box.setPadding(pad, dp(a, 8), pad, 0);
        final EditText old = passField(a, box, "Senha atual");
        final EditText nova = passField(a, box, "Nova senha (mínimo 8 caracteres)");
        final EditText conf = passField(a, box, "Confirmar nova senha");
        final AlertDialog d = new AlertDialog.Builder(a)
                .setTitle("Alterar senha")
                .setView(box)
                .setPositiveButton("Salvar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        d.show();
        d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n = nova.getText().toString();
            if (n.length() < 8) {
                nova.setError("Mínimo de 8 caracteres");
                return;
            }
            if (!n.equals(conf.getText().toString())) {
                conf.setError("As senhas não conferem");
                return;
            }
            if (!Repo.changePassword(a, userId, old.getText().toString(), n)) {
                old.setError("Senha atual incorreta");
                return;
            }
            d.dismiss();
            toast(a, "Senha alterada com sucesso");
        });
    }

    // ------------------------------------------------------------------ intents externas
    public static void openUrl(Activity a, String url) {
        if (url == null || url.trim().isEmpty()) return;
        String u = url.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
        start(a, new Intent(Intent.ACTION_VIEW, Uri.parse(u)));
    }

    public static void mail(Activity a, String email) {
        if (email == null || email.trim().isEmpty()) return;
        start(a, new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email.trim())));
    }

    public static void dial(Activity a, String phone) {
        if (phone == null || phone.trim().isEmpty()) return;
        start(a, new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.trim())));
    }

    private static void start(Activity a, Intent i) {
        try {
            a.startActivity(i);
        } catch (ActivityNotFoundException e) {
            toast(a, "Nenhum aplicativo encontrado para abrir");
        }
    }
}
