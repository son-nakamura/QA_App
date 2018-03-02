package jp.techacademy.takashi.nakamura.qa_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    DatabaseReference mDataBaseReference;  // Firebaseへの参照
    private EditText mNameText;  // 表示名変更のためのEditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Preferenceから表示名を取り出してEditTextに反映させる
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        mNameText = (EditText) findViewById(R.id.nameText);
        mNameText.setText(name);

        // Firebaseへの参照の初期設定
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        setTitle("設定");

        // UIの初期設定

        // ユーザーの表示名変更ボタンを押したときの処理
        Button changeButton = (Button) findViewById(R.id.changeButton);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出ていたら閉じる
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていない場合は警告を出して入力待ち(このActivityにとどまる)
                    Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // 変更した表示名をFirebaseに保存する
                String name = mNameText.getText().toString();  // EditTextから表示名を取得
                // databaseRoot -> users -> user.getUid() でユーザーのユニークIDへの参照を取得
                DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());
                Map<String, String> data = new HashMap<String, String>();  // HashMapの宣言
                data.put("name", name);  // 表示名をHashMapに追加
                // HashMapを取得したユーザーのユニークIDへの参照を使ってFirebaseに登録
                userRef.setValue(data);

                // 変更した表示名をPreferenceに保存する
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Const.NameKEY, name);
                editor.commit();

                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show();
            }
        });

        // ログアウトボタンを押したときの処理
        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebaseからログアウトする
                FirebaseAuth.getInstance().signOut();
                // 表示名EditTextを空にする
                mNameText.setText("");
                Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
