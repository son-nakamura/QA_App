package jp.techacademy.takashi.nakamura.qa_app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionSendActivity extends AppCompatActivity
        implements View.OnClickListener, DatabaseReference.CompletionListener {

    // Android6.0以降の場合に外部ストレージへの書き込みのパーミッションをユーザーに求めるとき使う定数
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    // 画像の選択または撮影のIntentを識別する定数
    private static final int CHOOSER_REQUEST_CODE = 100;

    private ProgressDialog mProgress;
    private EditText mTitleText;  // 質問タイトル
    private EditText mBodyText;  // 質問内容の文章
    private ImageView mImageView;  // 添付画像
    private Button mSendButton;  // 投稿ボタン

    private int mGenre;  // この質問のジャンル
    private Uri mPictureUri;  // 添付ファイルを取得するときに使うURI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_send);

        // QuestionDetailActivity(質問詳細画面)から渡ってきたIntentから
        // ジャンルの番号を取り出して保持する
        Bundle extras = getIntent().getExtras();
        mGenre = extras.getInt("genre");

        setTitle("質問作成");

        // UI の準備
        mTitleText = (EditText) findViewById(R.id.titleText);
        mBodyText = (EditText) findViewById(R.id.bodyText);

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");

    }

    // （１）showChooser()メソッドでIntentを送った結果得られる画像をmImageView表示するメソッド
    // startActivityForResult()の結果を受け取る
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_REQUEST_CODE) {
            // startActivityForResult()がCHOOSER_REQUEST_CODEを送った場合(常にtrue)
            if (resultCode != RESULT_OK) {
                // ユーザーがキャンセルしたり、画像の取得に失敗した場合
                if (mPictureUri != null) {
                    // Contentから外部ストレージへのアクセスであること指定を指定したURIを削除
                    getContentResolver().delete(mPictureUri, null, null);
                    // （メンバ変数の）画像のURIを削除
                    mPictureUri = null;
                }
                // 何もしないでreturn
                return;
            }

            // 受け取ったIntentがnull、またはIntentのデータがnullのとき、カメラで撮影した画像のURIをuriにセット
            // IntentがnullでなくIntentのデータもnullでない場合は、ギャラリーから取得した画像のURIをuriにセット
            Uri uri = (data == null || data.getData() == null) ? mPictureUri : data.getData();  // （１）

            // （１）で取得した方のURIからBitmapを取得する
            Bitmap image;
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                return;
            }

            // 取得したBitmapの長辺を500ピクセルにリサイズする
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight);

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizeImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);

            // BitmapをImageViewに表示する
            mImageView.setImageBitmap(resizeImage);

            // （メンバ変数の）画像のURIを削除
            mPictureUri = null;
        }
    }  // end of onActivityResult()

    // 添付画像または投稿ボタンが押されたときに呼ばれるリスナー
    @Override
    public void onClick(View v) {
        if (v == mImageView) {
            // 添付画像が押されたとき

            // パーミッションの状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android6.0以降の場合
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されているとき、画像を取得するメソッドを実行
                    showChooser();
                } else {
                    // 許可されていないとき、許可ダイアログを表示する
                    // ユーザーがダイアログを操作すると、PERMISSIONS_REQUEST_CODEで設定した値をセットして
                    // （２）onRequestPermissionsResult()が呼ばれる
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                    return;
                }
            } else {
                // Android5.x以前の場合、画像を取得するメソッドを実行
                showChooser();
            }
        } else if (v == mSendButton) {
            // 投稿ボタンが押されたとき

            // キーボードが出ていたら閉じる
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // Firebase内の参照の初期設定
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // databaseRoot -> contents -> genre と genreへの参照を取得
            DatabaseReference genreRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
            // HashMapの宣言
            Map<String, String> data = new HashMap<String, String>();

            // 現在ログインしているユーザーのユーザーIDをHashMapに追加
            data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

            // 質問のタイトルと質問内容の文章を取得する
            String title = mTitleText.getText().toString();
            String body = mBodyText.getText().toString();

            if (title.length() == 0) {
                // 質問のタイトルが入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (body.length() == 0) {
                // 質問内容の文章が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "質問を入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Preferenceから表示名を取得
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String name = sp.getString(Const.NameKEY, "");

            // 質問のタイトル、質問内容の文章、表示名をHashMapに追加
            data.put("title", title);
            data.put("body", body);
            data.put("name", name);

            //添付画像をImageVewから取得する
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();  // BitmapDrawableからBitmapに変換
                ByteArrayOutputStream baos = new ByteArrayOutputStream();  // ByteArrayOutputStreamの宣言
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);  // BitmapをJPEGに圧縮
                String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);  // Base64の文字列に変換
                // 文字列に変換した画像をHashMapに追加
                data.put("image", bitmapString);
            }
            // HashMapの全要素をFirebaseへ、genreへの参照を使って登録
            // Firebaseでの処理が終わると（３）onComplete()メソッドが呼ばれる
            genreRef.push().setValue(data, this);
            mProgress.show();
        }
    } // end of onClick()

    //（２）パーミッションを求めるダイアログをユーザーが操作すると呼ばれるリスナー
    @Override
    public void onRequestPermissionsResult(int requestCode, String perMissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {  // ダイアログを表示させたときのパーミッションコードかチェック
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき、画像を取得するメソッドを実行
                    showChooser();
                }
                // ユーザーが許可しなかったとき、何もしない
                return;
            }
        }
    }

    // Intentを発行して画像をカメラとギャラリーのどちらかから取得するメソッド
    private void showChooser() {
        // ギャラリーに渡すIntentの生成
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);  // Contentを取得するアクションを指定
        galleryIntent.setType("image/*");  // 画像を指定
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);  // 開くことができるものを指定

        // カメラで撮影するためのIntentの準備
        String filename = System.currentTimeMillis() + ".jpg";  // ファイル名を"時刻(ミリ秒).jpg"にする
        ContentValues values = new ContentValues();  // Contentのインスタンスを宣言
        values.put(MediaStore.Images.Media.TITLE, filename);  // Contentにファイル名をセット
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");  // ContentにMIMEタイプをセット
        // URIに外部ストレージへのアクセスであることとファイルのタイプ(values)をセットする
        mPictureUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // カメラに渡すIntentの生成
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  // 画像を撮影するアクションを指定
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);  // URIで指定した方法で画像を返すように指定

        // chooserIntentをgalleryIntentから作成
        Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");

        // chooserIntentにcameraIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        // chooserIntentを発行してアクション選択ダイアログを開く
        // chooserIntentの結果であることを識別するための定数CHOOSER_REQUEST_CODEを指定
        // 画像の取得が完了すると（１）onActivityResult()が呼ばれる
        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
    }

    //（３）HashMapの全要素のFirebaseへの登録が終了すると呼ばれるリスナー
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        mProgress.dismiss();

        if (databaseError == null) {
            // Firebaseにエラー無く登録できた場合、このActivityを閉じてMainActivityに戻る
            finish();
        } else {
            // なんらかのエラーが発生した
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }
}
