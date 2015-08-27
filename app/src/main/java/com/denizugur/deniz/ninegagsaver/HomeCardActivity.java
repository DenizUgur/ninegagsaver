package com.denizugur.deniz.ninegagsaver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.ArrayList;
import java.util.List;

public class HomeCardActivity extends AppCompatActivity {

    /**
     * Card contents:
     * photo
     * title
     * saved date
     * likes
     * comments
     * comments button
     * share
     *
     * FAB
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_card);

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        gagAdapter ca = new gagAdapter(createList(4));
        recList.setAdapter(ca);

    }

    private List<gagInfo> createList(int size) {

        List<gagInfo> result = new ArrayList<gagInfo>();
        for (int i=1; i <= size; i++) {
            gagInfo ci = new gagInfo();
            ci.photoId = gagInfo.PHOTO_PREFIX + i;
            ci.title = gagInfo.TITLE_PREFIX + i;
            ci.saved_date = gagInfo.SAVED_DATE_PREFIX + i;
            ci.likes = gagInfo.LIKES_PREFIX + i;
            ci.comments = gagInfo.COMMENTS_PREFIX + i;

            result.add(ci);
        }

        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_about) {
            new MaterialDialog.Builder(this)
                    .title("@string/action_about")
                    .content("will be added later")
                    .theme(Theme.DARK)
                    .cancelable(false)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}