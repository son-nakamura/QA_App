package jp.techacademy.takashi.nakamura.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

// QuestionDetailActivity(質問詳細画面)に表示するListViewのためのAdapter
public class QuestionDetailListAdapter extends BaseAdapter {
    private final static int TYPE_QUESTION = 0;  // 質問のViewであることを示す定数
    private final static int TYPE_ANSWER = 1;  // 回答のViewであることを示す定数

    private LayoutInflater mLayoutInflater = null;
    // 質問クラスのインスタンスを格納する変数、コンストラクタの第２引数で設定する
    private Question mQuestion;

    // コンストラクタ
    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQuestion = question;
    }

    @Override
    public int getCount() {
        return 1 + mQuestion.getAnswers().size();  // Viewの数は回答の数 + 1
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;  // 一番上だけ質問View
        } else {
            return TYPE_ANSWER;  // その他は回答View
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;  // 質問Viewと回答Viewの2種類
    }

    @Override
    public Object getItem(int position) {
        return mQuestion;  // 質問を返す
    }

    @Override
    public long getItemId(int position) {
        return 0;  // 質問のitemIdは0
    }

    // ListViewに表示される各View(convertView)を返すメソッド
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            // 質問Viewのとき
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detal, parent, false);
            }
            String body = mQuestion.getBody();  // 質問内容の文章
            String name = mQuestion.getName();  // 質問したユーザーの名前

            // 質問内容の文章をTextViewにセット
            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            // 質問したユーザーの名前をTextViewにセット
            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            // 画像を取り出しTextViewにセット
            byte[] bytes = mQuestion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
                        .copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

        } else {
            // 回答Viewのとき
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            // まず質問クラスのインスタンスから回答クラスのインスタンスを取り出す
            Answer answer = mQuestion.getAnswers().get(position - 1);

            String body = answer.getBody();  // 回答文章
            String name = answer.getName();  // 回答者名

            // 回答文章をTextViewにセット
            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            // 回答者名をTextViewにセット
            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}
