package com.estagiopro.app.util;

import java.util.Calendar;
import java.util.Locale;

/** Formatação de textos, datas e iniciais usada em todo o app. */
public final class Fmt {

    private Fmt() { }

    private static final String[] MES = {"jan", "fev", "mar", "abr", "mai", "jun",
            "jul", "ago", "set", "out", "nov", "dez"};
    public static final long DAY = 24L * 60 * 60 * 1000;

    public static String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h < 12) return "Bom dia,";
        if (h < 18) return "Boa tarde,";
        return "Boa noite,";
    }

    /** "há 2 dias", "há 3 h"... */
    public static String ago(long ts) {
        long d = System.currentTimeMillis() - ts;
        if (d < 0) d = 0;
        long min = d / 60000;
        if (min < 1) return "agora";
        if (min < 60) return "há " + min + " min";
        long h = min / 60;
        if (h < 24) return "há " + h + " h";
        long days = h / 24;
        if (days < 7) return "há " + days + (days == 1 ? " dia" : " dias");
        if (days < 30) {
            long w = days / 7;
            return "há " + w + (w == 1 ? " semana" : " semanas");
        }
        long m = days / 30;
        return "há " + m + (m == 1 ? " mês" : " meses");
    }

    /** "12 mai" */
    public static String dayMonth(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        return c.get(Calendar.DAY_OF_MONTH) + " " + MES[c.get(Calendar.MONTH)];
    }

    public static String time(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        return String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private static int dayIndex(Calendar c) {
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
    }

    /** "Hoje, 14:30" / "Amanhã, 14:30" / "20 nov, 14:30" */
    public static String interview(long ts) {
        Calendar now = Calendar.getInstance();
        Calendar t = Calendar.getInstance();
        t.setTimeInMillis(ts);
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        String d;
        if (dayIndex(t) == dayIndex(now)) d = "Hoje";
        else if (dayIndex(t) == dayIndex(tomorrow)) d = "Amanhã";
        else d = dayMonth(ts);
        return d + ", " + time(ts);
    }

    public static String fileSize(long bytes) {
        if (bytes <= 0) return "";
        Locale br = new Locale("pt", "BR");
        if (bytes >= 1048576) return String.format(br, "%.1f MB", bytes / 1048576.0);
        return String.format(br, "%d KB", Math.max(1, bytes / 1024));
    }

    // ------------------------------------------------------------ nomes
    private static String[] words(String s) {
        s = s == null ? "" : s.trim();
        if (s.isEmpty()) return new String[0];
        return s.split("\\s+");
    }

    private static String up(String s) {
        return s.toUpperCase(new Locale("pt", "BR"));
    }

    /** Iniciais do próprio usuário: "Ana Luiza Ferreira" -> "AL". */
    public static String initialsSelf(String name) {
        String[] w = words(name);
        if (w.length == 0) return "?";
        if (w.length >= 3) return up(w[0].substring(0, 1) + w[1].substring(0, 1));
        if (w.length == 2) return up(w[0].substring(0, 1) + w[1].substring(0, 1));
        return up(w[0].substring(0, Math.min(2, w[0].length())));
    }

    /** Iniciais em listas: primeiro + último nome ("Ana Luiza Ferreira" -> "AF"). */
    public static String initialsList(String name) {
        String[] w = words(name);
        if (w.length == 0) return "?";
        if (w.length == 1) return up(w[0].substring(0, Math.min(2, w[0].length())));
        return up(w[0].substring(0, 1) + w[w.length - 1].substring(0, 1));
    }

    /** "Ana Luiza Ferreira" -> "Ana Ferreira". */
    public static String shortName(String name) {
        String[] w = words(name);
        if (w.length <= 2) return name == null ? "" : name.trim();
        return w[0] + " " + w[w.length - 1];
    }

    /** Nome para saudação: "Ana Luiza Ferreira" -> "Ana Luiza". */
    public static String greetingName(String name) {
        String[] w = words(name);
        if (w.length >= 3) return w[0] + " " + w[1];
        if (w.length == 0) return "";
        return w[0];
    }

    public static String shortCompany(String name) {
        String[] w = words(name);
        if (w.length <= 1) return name == null ? "" : name.trim();
        String last = w[w.length - 1].toLowerCase(Locale.ROOT);
        if (last.equals("tecnologia") || last.equals("tecnologias") || last.equals("ltda")
                || last.equals("ltda.") || last.equals("s.a.") || last.equals("sa") || last.equals("eireli")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < w.length - 1; i++) {
                if (i > 0) sb.append(' ');
                sb.append(w[i]);
            }
            return sb.toString();
        }
        return name.trim();
    }

    /** Iniciais de uma organização a partir do nome ("Lumina Tecnologia" -> "LU", "Orbit Bank" -> "OB"). */
    public static String initialsOrg(String name) {
        String[] w = words(shortCompany(name));
        if (w.length == 0) return "?";
        if (w.length >= 2) return up(w[0].substring(0, 1) + w[1].substring(0, 1));
        return up(w[0].substring(0, Math.min(2, w[0].length())));
    }

    /** "Engenharia de Computação" -> "Eng. de Computação". */
    public static String abbrevCourse(String course) {
        if (course == null) return "";
        if (course.startsWith("Engenharia")) return "Eng." + course.substring("Engenharia".length());
        return course;
    }

    /** "Universidade de São Paulo" -> "USP"; "Mackenzie" continua igual. */
    public static String abbrevUni(String uni) {
        String[] w = words(uni);
        if (w.length <= 1) return uni == null ? "" : uni.trim();
        StringBuilder sb = new StringBuilder();
        for (String s : w) {
            if (Character.isUpperCase(s.charAt(0))) sb.append(s.charAt(0));
        }
        return sb.length() >= 2 ? sb.toString() : uni.trim();
    }

    public static String cityOf(String location) {
        if (location == null) return "";
        int i = location.indexOf(',');
        return (i > 0 ? location.substring(0, i) : location).trim();
    }

    public static String digits(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    /** 12345678000190 -> 12.345.678/0001-90 */
    public static String maskCnpj(String raw) {
        String d = digits(raw);
        if (d.length() > 14) d = d.substring(0, 14);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 2 || i == 5) sb.append('.');
            if (i == 8) sb.append('/');
            if (i == 12) sb.append('-');
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    public static boolean validEmail(String s) {
        return s != null && android.util.Patterns.EMAIL_ADDRESS.matcher(s.trim()).matches();
    }

    public static String plural(int n, String one, String many) {
        return n + " " + (n == 1 ? one : many);
    }

    /** Valida o CNPJ pelos dígitos verificadores. */
    public static boolean validCnpj(String raw) {
        String d = digits(raw);
        if (d.length() != 14) return false;
        boolean same = true;
        for (int i = 1; i < 14; i++) if (d.charAt(i) != d.charAt(0)) same = false;
        if (same) return false;
        int[] w1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] w2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int s = 0;
        for (int i = 0; i < 12; i++) s += (d.charAt(i) - '0') * w1[i];
        int r = s % 11;
        int d1 = r < 2 ? 0 : 11 - r;
        if (d1 != d.charAt(12) - '0') return false;
        s = 0;
        for (int i = 0; i < 13; i++) s += (d.charAt(i) - '0') * w2[i];
        r = s % 11;
        int d2 = r < 2 ? 0 : 11 - r;
        return d2 == d.charAt(13) - '0';
    }

    /** 5 -> "05" */
    public static String pad2(int n) {
        return String.format(Locale.US, "%02d", n);
    }

    /** 1248 -> "1.248" */
    public static String thousands(int n) {
        return java.text.NumberFormat.getInstance(new Locale("pt", "BR")).format(n);
    }

    /** "1800" -> "R$ 1.800"; texto livre é mantido. */
    public static String salary(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return "";
        if (s.matches("(?i)(r\\$\\s*)?[0-9.]+(,[0-9]{1,2})?")) {
            String num = s.replaceAll("(?i)r\\$\\s*", "").replace(".", "");
            String intPart = num.contains(",") ? num.substring(0, num.indexOf(',')) : num;
            String dec = num.contains(",") ? num.substring(num.indexOf(',')) : "";
            if (intPart.isEmpty()) return s;
            try {
                return "R$ " + thousands(Integer.parseInt(intPart)) + dec;
            } catch (NumberFormatException e) {
                return s;
            }
        }
        return s;
    }
}
