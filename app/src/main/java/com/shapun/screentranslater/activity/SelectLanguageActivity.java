package com.shapun.screentranslater.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mannan.translateapi.Language;
import com.shapun.screentranslater.R;
import com.shapun.screentranslater.adapter.SelectLanguageAdapter;
import com.shapun.screentranslater.preferences.Preferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectLanguageActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {
    private List<Language> mLanguages;
    private RecyclerView mRecyclerView;
    private SelectLanguageAdapter mSelectLanguageAdapter;

    @Override
    public void onCreate(Bundle savedInstamceState) {
        super.onCreate(savedInstamceState);
		//getTheme().applyStyle(R.style.AppTheme,true);
		getTheme().applyStyle(Preferences.getTheme(this),true);
        setContentView(R.layout.activity_select_language);
		findViewById(R.id.root).setClipToOutline(true);
        mLanguages = new ArrayList<>(Language.LANGUAGES);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSelectLanguageAdapter = new SelectLanguageAdapter(mLanguages);
        mRecyclerView.setAdapter(mSelectLanguageAdapter);
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);				
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        query = query.toLowerCase();
        mLanguages.clear();
        mSelectLanguageAdapter.notifyDataSetChanged();
        for (Language language : Language.LANGUAGES) {
            if (language.name.toLowerCase().contains(query)) {
                mLanguages.add(language);
            }
        }
        mSelectLanguageAdapter.notifyDataSetChanged();

        return true;
    }
	@Override
    public void onStart() {
        super.onStart();
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setLayout(width, height);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}
