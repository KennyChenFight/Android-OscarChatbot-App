package com.example.kenny.oscarchatbot;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<ChatMessage> {

    private Context context;

    public MessageAdapter(Activity context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        int layoutResource;
        ChatMessage chatMessage = getItem(position);
        // 決定講話人是誰
        if (chatMessage.getType() == ChatMessage.Type.PERSON) {
            layoutResource = R.layout.chat_right;
        } else {
            layoutResource = R.layout.chat_left;
        }

        convertView = inflater.inflate(layoutResource, parent, false);
        holder = new ViewHolder(convertView);
        // 設定講話內容、回答時間
        holder.msg.setText(chatMessage.getContent());
        holder.time.setText(chatMessage.getDateString());
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }

    private class ViewHolder {
        private TextView msg;
        private TextView time;

        public ViewHolder(View v) {
            msg = v.findViewById(R.id.txt_msg);
            time = v.findViewById(R.id.time);
        }
    }
}
