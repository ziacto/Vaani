/*

Copyright 2015, vyastech

This file is part of Vaani.

    Vaani is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Vaani is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Vaani.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.vyasware.vaani;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DoTheTrickActivity extends Activity {

    ArrayList<String> title;
    ArrayList<String> description;
    ItemAdapter adapter1;
    ListView list;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_the_trick);

        list = (ListView) findViewById(R.id.list);
        title = new ArrayList<String>();
        description = new ArrayList<String>();

        new Thread(new XMLNewsParser()).start();


    }


    class ItemAdapter extends BaseAdapter {

        final LayoutInflater mInflater;

        private class ViewHolder {
            public TextView title_text;
            public TextView des_text;
        }

        public ItemAdapter(Context context) {
            // TODO Auto-generated constructor stub
            super();
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        //@Override
        public int getCount() {
            return title.size();
        }

        //@Override
        public Object getItem(int position) {
            return position;
        }

        //@Override
        public long getItemId(int position) {
            return position;
        }

        //@Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.mainpage_listitem_activity, parent, false);
                holder = new ViewHolder();
                holder.title_text = (TextView) view.findViewById(R.id.title_text);
                holder.des_text = (TextView) view.findViewById(R.id.des_text);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.title_text.setText(""+title.get(position));

            holder.des_text.setText(""+Html.fromHtml(description.get(position)));

            return view;
        }
    }
}