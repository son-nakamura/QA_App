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

import java.util.ArrayList;

// MainActivityに表示するListViewのためのAdapter
public class QuestionsListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater = null;
    // 質問クラスQuestionのインスタンスを保持するArrayList
    private ArrayList<Question> mQuestionArrayList;

    // コンストラクタ
    public QuestionsListAdapter(Context context) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mQuestionArrayList.size();  // Viewの数
    }

    @Override
    public Object getItem(int position) {
        return mQuestionArrayList.get(position);  // ArrayList内のpositionの位置のQuestionを返す
    }

    @Override
    public long getItemId(int position) {
        return position;  // itemId == position
    }

    // ListViewに表示される各View(convertView)を返すメソッド
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_questions, parent, false);
        }

        // ArrayListからタイトルを取り出しTextViewにセット
        TextView titleText = (TextView) convertView.findViewById(R.id.titleTextView);
        titleText.setText(mQuestionArrayList.get(position).getTitle());

        // ArrayListから表示名を取り出しTextViewにセット
        TextView nameText = (TextView) convertView.findViewById(R.id.nameTextView);
        nameText.setText(mQuestionArrayList.get(position).getName());

        // ArrayListから回答の数を取り出しTextViewにセット
        TextView resText = (TextView) convertView.findViewById(R.id.resTextView);
        int resNum = mQuestionArrayList.get(position).getAnswers().size();
        resText.setText(String.valueOf(resNum));

        // ArrayListから画像を取り出しTextViewにセット
        byte[] bytes = mQuestionArrayList.get(position).getImageBytes();
        if (bytes.length != 0) {
            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
                    .copy(Bitmap.Config.ARGB_8888, true);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }

        return convertView;
    }

    // 引数で渡されたArrayListをこのアダプターのArrayListにセットする
    public void setQuestionArrayList(ArrayList<Question> questionArrayList) {
        mQuestionArrayList = questionArrayList;
    }
}
