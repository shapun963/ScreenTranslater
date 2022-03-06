package com.shapun.screentranslater.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.shapun.screentranslater.R;
import com.shapun.screentranslater.preferences.Preferences;
import com.shapun.screentranslater.service.ScreenTranslateService;
import com.shapun.screentranslater.theme.Theme;
import com.shapun.screentranslater.theme.ThemeAdapter;
import com.shapun.screentranslater.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SelectThemeBottomSheetDialogFragment extends BottomSheetDialogFragment
        implements View.OnClickListener {

    private static final int THEME_NONE = -2;
    private LinearLayout ll_auto;
    private LinearLayout ll_light;
    private LinearLayout ll_dark;
    private List<Theme> mThemeList;
    private RecyclerView rv_theme;
    private int mColorPrimary;
    private ThemeAdapter mThemeAdapter;
    private int mPreviousTheme = THEME_NONE;
    private static final float SELECTED_SCALE = (float) 0.95;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.dialog_select_theme, container, false);
        initViews(view);
        initThemes();
        reset();
        mColorPrimary =
                MaterialColors.getColor(
                        requireContext(),
                        android.R.attr.colorPrimary,
                        ContextCompat.getColor(requireContext(),R.color.colorPrimary));
		 if (bundle != null) {
            mPreviousTheme = bundle.getInt("prev_theme", THEME_NONE);
        }
        View selectedView = getView(AppCompatDelegate.getDefaultNightMode());
        selectedView.getBackground().setTint(mColorPrimary);
        if (mPreviousTheme != THEME_NONE) {
            view.post(() -> {
                 View prev_slected_view = getView(mPreviousTheme);
                 prev_slected_view.setScaleX(SELECTED_SCALE);
                 prev_slected_view.setScaleY(SELECTED_SCALE);
                 prev_slected_view.animate().scaleX(1).scaleY(1);
                 selectedView.animate().scaleX(SELECTED_SCALE).scaleY(SELECTED_SCALE);
            });
        }
        ll_auto.setOnClickListener(this);
        ll_light.setOnClickListener(this);
        ll_dark.setOnClickListener(this);
        return view;
    }

    private void reset() {
        ll_auto.getBackground().setTintList(null);
        ll_light.getBackground().setTintList(null);
        ll_dark.getBackground().setTintList(null);
        ll_auto.setScaleX(1);
        ll_auto.setScaleY(1);
        ll_light.setScaleX(1);
        ll_light.setScaleY(1);
        ll_dark.setScaleX(1);
        ll_dark.setScaleY(1);
    }

    @Override
    public void onClick(View view) {
        mPreviousTheme = AppCompatDelegate.getDefaultNightMode();
        View prevView = getView(AppCompatDelegate.getDefaultNightMode());
        prevView.getBackground().setTintList(null);
        prevView.animate().scaleX(1).scaleY(1);
        switch (view.getId()) {
            case R.id.ll_light:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.ll_dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.ll_auto:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default:
        }
        View selectedView = getView(AppCompatDelegate.getDefaultNightMode());
        selectedView.getBackground().setTint(mColorPrimary);
        selectedView.animate().scaleX(SELECTED_SCALE).scaleY(SELECTED_SCALE);
    }

    private View getView(int theme) {
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                return ll_light;
            case AppCompatDelegate.MODE_NIGHT_YES:
                return ll_dark;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                return ll_auto;
            default:
                return ll_auto;
        }
    }

    private void initThemes() {
        mThemeList = new ArrayList<>();
        mThemeList.add(new Theme(requireContext(),R.style.CustomThemeRed));
        mThemeList.add(new Theme(requireContext(),R.style.CustomThemePink));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemePurple));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeDeepPurple));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeIndigo));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeBlue));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeLightBlue));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeCyan));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeTeal));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeGreen));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeLightGreen));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeLime));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeYellow));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeAmber));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeOrange));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeDeepOrange));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeBrown));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeGrey));
		mThemeList.add(new Theme(requireContext(),R.style.CustomThemeBlueGrey));
		
        rv_theme.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mThemeAdapter = new ThemeAdapter(requireContext(), mThemeList);
        int theme = Preferences.getTheme(requireContext());
        for (int i = 0; i < mThemeList.size(); i++) {
            if (mThemeList.get(i).resId == theme) {
                mThemeAdapter.setSelectedTheme(i);
                break;
            }
        }
        mThemeAdapter.setRecyclerViewItemClickListener(
                pos -> {
                    Preferences.setTheme(requireContext(), mThemeList.get(pos));
                    mThemeAdapter.setSelectedTheme(pos);
					Intent intent = new Intent(requireContext(), ScreenTranslateService.class);
                    intent.setAction(ScreenTranslateService.ACTION_SHOW_NOTIFICATION);
                    requireContext().startService(intent);
                    requireActivity().recreate();
                });
        rv_theme.setAdapter(mThemeAdapter);
    }

    private void initViews(View container) {
        ll_auto = container.findViewById(R.id.ll_auto);
        ll_light = container.findViewById(R.id.ll_light);
        ll_dark = container.findViewById(R.id.ll_dark);
        rv_theme = container.findViewById(R.id.rv_select_theme);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("prev_theme", mPreviousTheme);
        super.onSaveInstanceState(outState);
    }
}
