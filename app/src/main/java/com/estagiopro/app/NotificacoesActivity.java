package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Notice;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class NotificacoesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);
        Ui.statusBar(this, R.color.bg, true);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnMarcarLidas).setOnClickListener(v -> {
            Repo.markAllRead(this, me.id);
            bind();
        });
    }

    @Override
    protected void bind() {
        LinearLayout list = findViewById(R.id.llNotificacoes);
        list.removeAllViews();
        List<Notice> notices = Repo.notices(this, me.id);
        findViewById(R.id.btnMarcarLidas).setVisibility(Repo.unread(this, me.id) > 0 ? View.VISIBLE : View.INVISIBLE);
        if (notices.isEmpty()) {
            Ui.empty(list, "Você não tem notificações.");
            return;
        }
        for (Notice n : notices) {
            View row = Ui.inflate(this, R.layout.item_notification, list);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(n.title);
            ((TextView) row.findViewById(R.id.tvBody)).setText(n.body);
            ((TextView) row.findViewById(R.id.tvTime)).setText(Fmt.ago(n.createdAt));
            row.findViewById(R.id.viewUnread).setVisibility(n.read ? View.GONE : View.VISIBLE);
            list.addView(row);
        }
    }
}
