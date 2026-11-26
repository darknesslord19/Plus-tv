// ============================================================
//   Project Name  : SUKA FILM
//   Author        : Celine Olivia Clarissa
//   Studio        : Olivera Studio
//   Copyright (c) : 2026 Olivera Studio. All rights reserved.
//   Description   : Turning coffee into code, one line at a time.
// ============================================================
//   Terms & Conditions :
//   - This code is NOT for sale or commercial redistribution.
//   - Modifying, copying, or redistributing any part of this
//     code requires prior written permission from the developer.
//   - For inquiries, please contact the author directly.
// ============================================================
//   Crafted with logic, love, and a dash of magic.
// ============================================================

package com.oliverastudio.sukafilm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
PlaylistManager.OnMoviesDataChangedListener,
FavoritesHistoryManager.OnUserDataChangedListener {
	
	private static final int TAB_HOME = 0;
	private static final int TAB_EXPLORE = 1;
	private static final int TAB_FAVORITES = 2;
	private static final int TAB_DOWNLOADS = 3;
	
	private int currentTab = TAB_HOME;
	
	private ScrollView containerTabHome;
	private LinearLayout containerTabExplore;
	private LinearLayout containerTabFavorites;
	private LinearLayout containerTabDownloads;
	
	private LinearLayout navItemHome, navItemExplore, navItemFavorites, navItemDownloads;
	private ImageView imgNavHome, imgNavExplore, imgNavFavorites, imgNavDownloads;
	private TextView txtNavHome, txtNavExplore, txtNavFavorites, txtNavDownloads;
	private View indicatorNavHome, indicatorNavExplore, indicatorNavFavorites, indicatorNavDownloads;
	private TextView txtNavDownloadBadge;
	
	private FrameLayout btnTopSearch;
	private LinearLayout layoutSearchBox;
	private EditText edtSearch;
	private ImageView btnClearSearch;
	private TextView txtFirebaseStatus;
	private View dotFirebaseStatus;
	
	private FrameLayout layoutBannerContainer;
	private ViewPager2 viewPagerBanner;
	private LinearLayout layoutDots;
	private LinearLayout layoutGenreChips;
	private LinearLayout layoutEmptyFirebase;
	private Button btnRetryFirebase;
	
	private LinearLayout layoutSectionGenres;
	private LinearLayout layoutSectionSpotlight;
	private LinearLayout layoutSectionTrending;
	private LinearLayout layoutSectionTopRated;
	private LinearLayout layoutSectionPopular;
	
	private CardView cardSpotlight;
	private ImageView imgSpotlightBackdrop;
	private TextView txtSpotlightTitle;
	private TextView txtSpotlightRating;
	private TextView txtSpotlightMeta;
	private Movie currentSpotlightMovie;
	
	private RecyclerView recyclerTrending;
	private RecyclerView recyclerTopRated;
	private RecyclerView recyclerPopular;
	private TextView txtMovieCount;
	private LinearLayout layoutEmptySearch;
	
	private LinearLayout btnPickerCountry;
	private TextView txtPickerCountryValue;
	private LinearLayout btnPickerYear;
	private TextView txtPickerYearValue;
	private LinearLayout layoutFilterRatings;
	private TextView btnResetFilter;
	private TextView txtExploreResultCount;
	private RecyclerView recyclerExplore;
	private LinearLayout layoutEmptyExplore;
	
	private String selectedCountry = "Semua";
	private String selectedYear = "Semua";
	private String selectedRating = "Semua";
	
	private TextView btnSubtabBookmarks;
	private TextView btnSubtabHistory;
	private LinearLayout layoutHistoryHeader;
	private TextView btnClearHistory;
	private RecyclerView recyclerFavorites;
	private LinearLayout layoutEmptyFavorites;
	private ImageView imgEmptyFavIcon;
	private TextView txtEmptyFavTitle;
	private TextView txtEmptyFavDesc;
	private boolean isBookmarkSubtabActive = true;
	
	private TextView txtTabFolderPath;
	private TextView txtTabDownloadsCount;
	private RecyclerView recyclerTabDownloads;
	private LinearLayout layoutEmptyTabDownloads;
	
	private PlaylistManager realtimeManager;
	private DownloadManagerHelper downloadHelper;
	private FavoritesHistoryManager favoritesManager;
	
	private BannerParallaxAdapter bannerAdapter;
	private TrendingCardAdapter trendingAdapter;
	private MovieCardAdapter topRatedAdapter;
	private MovieCardAdapter popularAdapter;
	private MovieCardAdapter exploreAdapter;
	private MovieCardAdapter favoritesAdapter;
	private DownloadAdapter tabDownloadAdapter;
	
	private List<Movie> featuredMovies = new ArrayList<>();
	private final Handler autoSlideHandler = new Handler(Looper.getMainLooper());
	private Runnable autoSlideRunnable;
	private static final long SLIDE_DELAY_MS = 3500;
	private String selectedGenre = "Semua";
	private List<String> currentGenresList = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);
		getWindow().getDecorView().setSystemUiVisibility(
		View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		);
		
		realtimeManager = PlaylistManager.getInstance();
		downloadHelper = new DownloadManagerHelper(this);
		favoritesManager = FavoritesHistoryManager.getInstance(this);
		
		initViews();
		setupBottomNavigation();
		setupParallaxBanner();
		setupHomeSections();
		setupSearch();
		setupExploreTab();
		setupFavoritesTab();
		setupDownloadsTab();
		
		realtimeManager.addListener(this);
		favoritesManager.addListener(this);
		
		switchTab(TAB_HOME);
	}
	
	private void initViews() {
		containerTabHome = findViewById(R.id.container_tab_home);
		containerTabExplore = findViewById(R.id.container_tab_explore);
		containerTabFavorites = findViewById(R.id.container_tab_favorites);
		containerTabDownloads = findViewById(R.id.container_tab_downloads);
		
		navItemHome = findViewById(R.id.nav_item_home);
		navItemExplore = findViewById(R.id.nav_item_explore);
		navItemFavorites = findViewById(R.id.nav_item_favorites);
		navItemDownloads = findViewById(R.id.nav_item_downloads);
		
		imgNavHome = findViewById(R.id.img_nav_home);
		imgNavExplore = findViewById(R.id.img_nav_explore);
		imgNavFavorites = findViewById(R.id.img_nav_favorites);
		imgNavDownloads = findViewById(R.id.img_nav_downloads);
		
		txtNavHome = findViewById(R.id.txt_nav_home);
		txtNavExplore = findViewById(R.id.txt_nav_explore);
		txtNavFavorites = findViewById(R.id.txt_nav_favorites);
		txtNavDownloads = findViewById(R.id.txt_nav_downloads);
		
		indicatorNavHome = findViewById(R.id.indicator_nav_home);
		indicatorNavExplore = findViewById(R.id.indicator_nav_explore);
		indicatorNavFavorites = findViewById(R.id.indicator_nav_favorites);
		indicatorNavDownloads = findViewById(R.id.indicator_nav_downloads);
		
		txtNavDownloadBadge = findViewById(R.id.txt_nav_download_badge);
		
		btnTopSearch = findViewById(R.id.btn_top_search);
		layoutSearchBox = findViewById(R.id.layout_search_box);
		edtSearch = findViewById(R.id.edt_search);
		btnClearSearch = findViewById(R.id.btn_clear_search);
		txtFirebaseStatus = findViewById(R.id.txt_firebase_status);
		dotFirebaseStatus = findViewById(R.id.dot_firebase_status);
		
		layoutBannerContainer = findViewById(R.id.layout_banner_container);
		viewPagerBanner = findViewById(R.id.viewpager_parallax);
		layoutDots = findViewById(R.id.layout_dots);
		layoutGenreChips = findViewById(R.id.layout_genre_chips);
		layoutEmptyFirebase = findViewById(R.id.layout_empty_firebase);
		btnRetryFirebase = findViewById(R.id.btn_retry_firebase);
		
		layoutSectionGenres = findViewById(R.id.layout_section_genres);
		layoutSectionSpotlight = findViewById(R.id.layout_section_spotlight);
		layoutSectionTrending = findViewById(R.id.layout_section_trending);
		layoutSectionTopRated = findViewById(R.id.layout_section_top_rated);
		layoutSectionPopular = findViewById(R.id.layout_section_popular);
		
		cardSpotlight = findViewById(R.id.card_spotlight);
		imgSpotlightBackdrop = findViewById(R.id.img_spotlight_backdrop);
		txtSpotlightTitle = findViewById(R.id.txt_spotlight_title);
		txtSpotlightRating = findViewById(R.id.txt_spotlight_rating);
		txtSpotlightMeta = findViewById(R.id.txt_spotlight_meta);
		
		recyclerTrending = findViewById(R.id.recycler_trending);
		recyclerTopRated = findViewById(R.id.recycler_top_rated);
		recyclerPopular = findViewById(R.id.recycler_popular);
		txtMovieCount = findViewById(R.id.txt_movie_count);
		layoutEmptySearch = findViewById(R.id.layout_empty_search);
		
		btnPickerCountry = findViewById(R.id.btn_picker_country);
		txtPickerCountryValue = findViewById(R.id.txt_picker_country_value);
		btnPickerYear = findViewById(R.id.btn_picker_year);
		txtPickerYearValue = findViewById(R.id.txt_picker_year_value);
		layoutFilterRatings = findViewById(R.id.layout_filter_ratings);
		btnResetFilter = findViewById(R.id.btn_reset_filter);
		txtExploreResultCount = findViewById(R.id.txt_explore_result_count);
		recyclerExplore = findViewById(R.id.recycler_explore);
		layoutEmptyExplore = findViewById(R.id.layout_empty_explore);
		
		btnSubtabBookmarks = findViewById(R.id.btn_subtab_bookmarks);
		btnSubtabHistory = findViewById(R.id.btn_subtab_history);
		layoutHistoryHeader = findViewById(R.id.layout_history_header);
		btnClearHistory = findViewById(R.id.btn_clear_history);
		recyclerFavorites = findViewById(R.id.recycler_favorites);
		layoutEmptyFavorites = findViewById(R.id.layout_empty_favorites);
		imgEmptyFavIcon = findViewById(R.id.img_empty_fav_icon);
		txtEmptyFavTitle = findViewById(R.id.txt_empty_fav_title);
		txtEmptyFavDesc = findViewById(R.id.txt_empty_fav_desc);
		
		txtTabFolderPath = findViewById(R.id.txt_tab_folder_path);
		txtTabDownloadsCount = findViewById(R.id.txt_tab_downloads_count);
		recyclerTabDownloads = findViewById(R.id.recycler_tab_downloads);
		layoutEmptyTabDownloads = findViewById(R.id.layout_empty_tab_downloads);
	}
	
	private void setupBottomNavigation() {
		navItemHome.setOnClickListener(v -> switchTab(TAB_HOME));
		navItemExplore.setOnClickListener(v -> switchTab(TAB_EXPLORE));
		navItemFavorites.setOnClickListener(v -> switchTab(TAB_FAVORITES));
		navItemDownloads.setOnClickListener(v -> switchTab(TAB_DOWNLOADS));
	}
	
	private void switchTab(int tabIndex) {
		currentTab = tabIndex;
		
		containerTabHome.setVisibility(tabIndex == TAB_HOME ? View.VISIBLE : View.GONE);
		containerTabExplore.setVisibility(tabIndex == TAB_EXPLORE ? View.VISIBLE : View.GONE);
		containerTabFavorites.setVisibility(tabIndex == TAB_FAVORITES ? View.VISIBLE : View.GONE);
		containerTabDownloads.setVisibility(tabIndex == TAB_DOWNLOADS ? View.VISIBLE : View.GONE);
		
		updateNavItemStyle(navItemHome, imgNavHome, txtNavHome, indicatorNavHome, tabIndex == TAB_HOME);
		updateNavItemStyle(navItemExplore, imgNavExplore, txtNavExplore, indicatorNavExplore, tabIndex == TAB_EXPLORE);
		updateNavItemStyle(navItemFavorites, imgNavFavorites, txtNavFavorites, indicatorNavFavorites, tabIndex == TAB_FAVORITES);
		updateNavItemStyle(navItemDownloads, imgNavDownloads, txtNavDownloads, indicatorNavDownloads, tabIndex == TAB_DOWNLOADS);
		
		if (tabIndex == TAB_EXPLORE) {
			applyExploreFilters();
		} else if (tabIndex == TAB_FAVORITES) {
			refreshFavoritesTab();
		} else if (tabIndex == TAB_DOWNLOADS) {
			refreshDownloadsTab();
		}
	}
	
	private void updateNavItemStyle(LinearLayout item, ImageView icon, TextView label, View indicator, boolean isSelected) {
		int color = isSelected ? Color.parseColor("#FACC15") : Color.parseColor("#94A3B8");
		icon.setColorFilter(color);
		label.setTextColor(color);
		label.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
		indicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
	}
	
	private void setupParallaxBanner() {
		featuredMovies = getFeaturedList(realtimeManager.getCurrentMovies());
		bannerAdapter = new BannerParallaxAdapter(this, featuredMovies, this::openMovieDetail);
		viewPagerBanner.setAdapter(bannerAdapter);
		viewPagerBanner.setPageTransformer(new Parallax3DTransformer());
		viewPagerBanner.setOffscreenPageLimit(3);
		
		setupBannerDots(featuredMovies.size());
		
		viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				updateBannerDots(position);
			}
		});
		
		startAutoSlide();
	}
	
	private void setupHomeSections() {
		recyclerTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		trendingAdapter = new TrendingCardAdapter(this, this::openMovieDetail);
		recyclerTrending.setAdapter(trendingAdapter);
		
		recyclerTopRated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		topRatedAdapter = new MovieCardAdapter(this, new ArrayList<>(), false, this::openMovieDetail);
		recyclerTopRated.setAdapter(topRatedAdapter);
		
		recyclerPopular.setLayoutManager(new GridLayoutManager(this, 2));
		popularAdapter = new MovieCardAdapter(this, new ArrayList<>(), true, this::openMovieDetail);
		recyclerPopular.setAdapter(popularAdapter);
		
		cardSpotlight.setOnClickListener(v -> {
			if (currentSpotlightMovie != null) {
				openMovieDetail(currentSpotlightMovie);
			}
		});
		
		btnRetryFirebase.setOnClickListener(v -> {
			Toast.makeText(this, "Memuat ulang playlist M3U...", Toast.LENGTH_SHORT).show();
			realtimeManager.reload();
		});
	}
	
	private void setupSearch() {
		btnTopSearch.setOnClickListener(v -> {
			if (layoutSearchBox.getVisibility() == View.VISIBLE) {
				layoutSearchBox.setVisibility(View.GONE);
			} else {
				layoutSearchBox.setVisibility(View.VISIBLE);
				edtSearch.requestFocus();
			}
		});
		
		edtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String query = s.toString().trim();
				btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
				performHomeSearch(query);
			}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		btnClearSearch.setOnClickListener(v -> {
			edtSearch.setText("");
			performHomeSearch("");
		});
	}
	
	private void performHomeSearch(String query) {
		List<Movie> all = realtimeManager.getCurrentMovies();
		List<Movie> results = new ArrayList<>();
		if (query.isEmpty()) {
			filterMoviesByGenre(selectedGenre);
			return;
		}
		
		String lower = query.toLowerCase().trim();
		for (Movie m : all) {
			boolean matchTitle = m.getTitle() != null && m.getTitle().toLowerCase().contains(lower);
			boolean matchGenre = m.getGenre() != null && m.getGenre().toLowerCase().contains(lower);
			boolean matchCast = m.getCast() != null && m.getCast().toLowerCase().contains(lower);
			boolean matchDirector = m.getDirector() != null && m.getDirector().toLowerCase().contains(lower);
			boolean matchYear = String.valueOf(m.getYear()).contains(lower);
			if (matchTitle || matchGenre || matchCast || matchDirector || matchYear) {
				results.add(m);
			}
		}
		
		popularAdapter.updateList(results);
		txtMovieCount.setText(results.size() + " Film");
		
		if (results.isEmpty()) {
			layoutEmptySearch.setVisibility(View.VISIBLE);
			recyclerPopular.setVisibility(View.GONE);
		} else {
			layoutEmptySearch.setVisibility(View.GONE);
			recyclerPopular.setVisibility(View.VISIBLE);
		}
	}
	
	private void filterMoviesByGenre(String genre) {
		selectedGenre = genre;
		List<Movie> all = realtimeManager.getCurrentMovies();
		List<Movie> filtered = new ArrayList<>();
		
		if (genre == null || genre.equalsIgnoreCase("Semua")) {
			filtered.addAll(all);
		} else {
			for (Movie m : all) {
				if (m.getGenre() != null && m.getGenre().equalsIgnoreCase(genre)) {
					filtered.add(m);
				}
			}
		}
		
		popularAdapter.updateList(filtered);
		txtMovieCount.setText(filtered.size() + " Film");
		layoutEmptySearch.setVisibility(filtered.isEmpty() && !all.isEmpty() ? View.VISIBLE : View.GONE);
		recyclerPopular.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
	}
	
	private void setupExploreTab() {
		recyclerExplore.setLayoutManager(new GridLayoutManager(this, 2));
		exploreAdapter = new MovieCardAdapter(this, new ArrayList<>(), true, this::openMovieDetail);
		recyclerExplore.setAdapter(exploreAdapter);
		
		updateFilterPickerLabels();
		setupRatingChips();
		
		btnPickerCountry.setOnClickListener(v -> showCountryPickerDialog());
		btnPickerYear.setOnClickListener(v -> showYearPickerDialog());
		
		btnResetFilter.setOnClickListener(v -> {
			selectedCountry = "Semua";
			selectedYear = "Semua";
			selectedRating = "Semua";
			updateFilterPickerLabels();
			setupRatingChips();
			applyExploreFilters();
			Toast.makeText(this, "Filter telah direset ke Semua", Toast.LENGTH_SHORT).show();
		});
	}
	
	private void updateFilterPickerLabels() {
		if (txtPickerCountryValue != null) {
			txtPickerCountryValue.setText(selectedCountry.equalsIgnoreCase("Semua") ? "Semua Negara" : selectedCountry);
		}
		if (txtPickerYearValue != null) {
			txtPickerYearValue.setText(selectedYear.equalsIgnoreCase("Semua") ? "Semua Tahun" : "Tahun " + selectedYear);
		}
	}
	
	private void showYearPickerDialog() {
		Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_year_picker_slider);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		
		TextView txtYearDisplay = dialog.findViewById(R.id.txt_dialog_year_display);
		SeekBar seekBarYear = dialog.findViewById(R.id.seekbar_year);
		ImageView btnClose = dialog.findViewById(R.id.btn_close_year_dialog);
		Button btnResetAll = dialog.findViewById(R.id.btn_dialog_year_reset);
		Button btnApply = dialog.findViewById(R.id.btn_dialog_year_apply);
		
		TextView chipAll = dialog.findViewById(R.id.btn_chip_year_all);
		TextView chip2026 = dialog.findViewById(R.id.btn_chip_year_2026);
		TextView chip2025 = dialog.findViewById(R.id.btn_chip_year_2025);
		TextView chip2024 = dialog.findViewById(R.id.btn_chip_year_2024);
		TextView chip2023 = dialog.findViewById(R.id.btn_chip_year_2023);
		TextView chip2022 = dialog.findViewById(R.id.btn_chip_year_2022);
		
		final String[] tempYear = {selectedYear};
		
		if (selectedYear.equalsIgnoreCase("Semua")) {
			txtYearDisplay.setText("Semua Tahun");
			seekBarYear.setProgress(34);
		} else {
			try {
				int y = Integer.parseInt(selectedYear);
				int progress = Math.max(0, Math.min(36, y - 1990));
				seekBarYear.setProgress(progress);
				txtYearDisplay.setText("Tahun " + y);
			} catch (Exception e) {
				txtYearDisplay.setText("Semua Tahun");
			}
		}
		
		seekBarYear.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					int calcYear = 1990 + progress;
					tempYear[0] = String.valueOf(calcYear);
					txtYearDisplay.setText("Tahun " + calcYear);
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		View.OnClickListener presetListener = v -> {
			if (v == chipAll) {
				tempYear[0] = "Semua";
				txtYearDisplay.setText("Semua Tahun");
			} else if (v instanceof TextView) {
				String yStr = ((TextView) v).getText().toString();
				tempYear[0] = yStr;
				txtYearDisplay.setText("Tahun " + yStr);
				try {
					int y = Integer.parseInt(yStr);
					seekBarYear.setProgress(Math.max(0, Math.min(36, y - 1990)));
				} catch (Exception ignored) {}
			}
		};
		
		if (chipAll != null) chipAll.setOnClickListener(presetListener);
		if (chip2026 != null) chip2026.setOnClickListener(presetListener);
		if (chip2025 != null) chip2025.setOnClickListener(presetListener);
		if (chip2024 != null) chip2024.setOnClickListener(presetListener);
		if (chip2023 != null) chip2023.setOnClickListener(presetListener);
		if (chip2022 != null) chip2022.setOnClickListener(presetListener);
		
		if (btnResetAll != null) {
			btnResetAll.setOnClickListener(v -> {
				tempYear[0] = "Semua";
				txtYearDisplay.setText("Semua Tahun");
			});
		}
		
		if (btnClose != null) {
			btnClose.setOnClickListener(v -> dialog.dismiss());
		}
		
		if (btnApply != null) {
			btnApply.setOnClickListener(v -> {
				selectedYear = tempYear[0];
				updateFilterPickerLabels();
				applyExploreFilters();
				dialog.dismiss();
			});
		}
		
		dialog.show();
	}
	
	private void showCountryPickerDialog() {
		Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_country_picker);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		
		ImageView btnClose = dialog.findViewById(R.id.btn_close_country_dialog);
		EditText edtSearch = dialog.findViewById(R.id.edt_search_dialog_country);
		LinearLayout countryContainer = dialog.findViewById(R.id.layout_dialog_country_container);
		Button btnAll = dialog.findViewById(R.id.btn_dialog_country_all);
		Button btnApply = dialog.findViewById(R.id.btn_dialog_country_apply);
		
		final String[][] countryData = {
			{"Semua", "Semua Negara", "🌐", "Seluruh negara asal film"},
			{"Indonesia", "Indonesia", "🇮🇩", "Film dan serial lokal Indonesia"},
			{"Korea", "Korea Selatan", "🇰🇷", "K-Drama dan sinema Korea"},
			{"Jepang", "Jepang", "🇯🇵", "Anime dan film bioskop Jepang"},
			{"Barat", "Barat (Hollywood)", "🎬", "Film internasional / Amerika & Eropa"},
			{"Thailand", "Thailand", "🇹🇭", "Film komedi dan horor Thailand"},
			{"China", "China (Mandarin)", "🇨🇳", "Wuxia, aksi, dan drama Mandarin"},
			{"India", "India (Bollywood)", "🇮🇳", "Film Bollywood dan aksi India"},
			{"Inggris", "Inggris (UK)", "🇬🇧", "Film drama dan thriller Inggris"}
		};
		
		final String[] tempCountry = {selectedCountry};
		
		Runnable renderList = () -> {
			countryContainer.removeAllViews();
			String query = edtSearch != null ? edtSearch.getText().toString().trim().toLowerCase() : "";
			
			for (String[] c : countryData) {
				String code = c[0];
				String name = c[1];
				String emoji = c[2];
				String desc = c[3];
				
				if (!query.isEmpty() && !name.toLowerCase().contains(query) && !desc.toLowerCase().contains(query)) {
					continue;
				}
				
				View row = getLayoutInflater().inflate(R.layout.item_country_picker, countryContainer, false);
				TextView txtFlag = row.findViewById(R.id.txt_country_flag_emoji);
				TextView txtName = row.findViewById(R.id.txt_country_name);
				TextView txtSub = row.findViewById(R.id.txt_country_sub);
				ImageView imgCheck = row.findViewById(R.id.img_country_selected_check);
				
				txtFlag.setText(emoji);
				txtName.setText(name);
				txtSub.setText(desc);
				
				boolean isSelected = code.equalsIgnoreCase(tempCountry[0]);
				if (isSelected) {
					row.setBackgroundResource(R.drawable.bg_card_dark);
					imgCheck.setVisibility(View.VISIBLE);
					txtName.setTextColor(Color.parseColor("#FACC15"));
				} else {
					row.setBackgroundResource(R.drawable.bg_chip_unselected);
					imgCheck.setVisibility(View.GONE);
					txtName.setTextColor(Color.parseColor("#FFFFFF"));
				}
				
				row.setOnClickListener(v -> {
					tempCountry[0] = code;
					selectedCountry = code;
					updateFilterPickerLabels();
					applyExploreFilters();
					dialog.dismiss();
				});
				
				countryContainer.addView(row);
			}
		};
		
		renderList.run();
		
		if (edtSearch != null) {
			edtSearch.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					renderList.run();
				}
				
				@Override
				public void afterTextChanged(Editable s) {}
			});
		}
		
		if (btnAll != null) {
			btnAll.setOnClickListener(v -> {
				selectedCountry = "Semua";
				updateFilterPickerLabels();
				applyExploreFilters();
				dialog.dismiss();
			});
		}
		
		if (btnApply != null) {
			btnApply.setOnClickListener(v -> {
				selectedCountry = tempCountry[0];
				updateFilterPickerLabels();
				applyExploreFilters();
				dialog.dismiss();
			});
		}
		
		if (btnClose != null) {
			btnClose.setOnClickListener(v -> dialog.dismiss());
		}
		
		dialog.show();
	}
	
	private void setupRatingChips() {
		String[] ratings = {"Semua", "8.5+", "8.0+", "7.0+"};
		layoutFilterRatings.removeAllViews();
		for (String r : ratings) {
			TextView chip = createFilterChip(r, r.equalsIgnoreCase(selectedRating));
			chip.setOnClickListener(v -> {
				selectedRating = r;
				setupRatingChips();
				applyExploreFilters();
			});
			layoutFilterRatings.addView(chip);
		}
	}
	
	private TextView createFilterChip(String text, boolean isSelected) {
		TextView chip = new TextView(this);
		chip.setText(text);
		chip.setTextSize(11f);
		chip.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT,
		LinearLayout.LayoutParams.WRAP_CONTENT
		);
		params.setMargins(0, 0, dpToPx(6), 0);
		chip.setLayoutParams(params);
		
		if (isSelected) {
			chip.setBackgroundResource(R.drawable.bg_chip_selected);
			chip.setTextColor(0xFF0B0E14);
			chip.setTypeface(null, Typeface.BOLD);
		} else {
			chip.setBackgroundResource(R.drawable.bg_chip_unselected);
			chip.setTextColor(0xFF94A3B8);
			chip.setTypeface(null, Typeface.NORMAL);
		}
		return chip;
	}
	
	private void applyExploreFilters() {
		List<Movie> all = realtimeManager.getCurrentMovies();
		List<Movie> filtered = new ArrayList<>();
		
		for (Movie m : all) {
			boolean matchCountry = selectedCountry.equalsIgnoreCase("Semua") ||
			(m.getCountry() != null && m.getCountry().equalsIgnoreCase(selectedCountry));
			
			boolean matchYear = selectedYear.equalsIgnoreCase("Semua") ||
			String.valueOf(m.getYear()).equalsIgnoreCase(selectedYear);
			
			boolean matchRating = true;
			if (selectedRating.equals("8.5+")) {
				matchRating = m.getRating() >= 8.5;
			} else if (selectedRating.equals("8.0+")) {
				matchRating = m.getRating() >= 8.0;
			} else if (selectedRating.equals("7.0+")) {
				matchRating = m.getRating() >= 7.0;
			}
			
			if (matchCountry && matchYear && matchRating) {
				filtered.add(m);
			}
		}
		
		exploreAdapter.updateList(filtered);
		txtExploreResultCount.setText("Hasil Filter: " + filtered.size() + " Film");
		
		if (filtered.isEmpty()) {
			layoutEmptyExplore.setVisibility(View.VISIBLE);
			recyclerExplore.setVisibility(View.GONE);
		} else {
			layoutEmptyExplore.setVisibility(View.GONE);
			recyclerExplore.setVisibility(View.VISIBLE);
		}
	}
	
	private void setupFavoritesTab() {
		recyclerFavorites.setLayoutManager(new GridLayoutManager(this, 2));
		favoritesAdapter = new MovieCardAdapter(this, new ArrayList<>(), true, this::openMovieDetail);
		recyclerFavorites.setAdapter(favoritesAdapter);
		
		btnSubtabBookmarks.setOnClickListener(v -> {
			isBookmarkSubtabActive = true;
			btnSubtabBookmarks.setBackgroundResource(R.drawable.bg_btn_gold);
			btnSubtabBookmarks.setTextColor(0xFF0B0E14);
			btnSubtabHistory.setBackgroundResource(R.drawable.bg_btn_dark);
			btnSubtabHistory.setTextColor(0xFF94A3B8);
			layoutHistoryHeader.setVisibility(View.GONE);
			refreshFavoritesTab();
		});
		
		btnSubtabHistory.setOnClickListener(v -> {
			isBookmarkSubtabActive = false;
			btnSubtabHistory.setBackgroundResource(R.drawable.bg_btn_gold);
			btnSubtabHistory.setTextColor(0xFF0B0E14);
			btnSubtabBookmarks.setBackgroundResource(R.drawable.bg_btn_dark);
			btnSubtabBookmarks.setTextColor(0xFF94A3B8);
			layoutHistoryHeader.setVisibility(View.VISIBLE);
			refreshFavoritesTab();
		});
		
		btnClearHistory.setOnClickListener(v -> {
			new AlertDialog.Builder(this)
			.setTitle("Hapus Riwayat Nonton")
			.setMessage("Apakah Anda yakin ingin menghapus semua riwayat tonton film?")
			.setPositiveButton("Hapus", (dialog, which) -> {
				favoritesManager.clearHistory();
				refreshFavoritesTab();
				Toast.makeText(this, "Riwayat tonton telah dibersihkan", Toast.LENGTH_SHORT).show();
			})
			.setNegativeButton("Batal", null)
			.show();
		});
	}
	
	private void refreshFavoritesTab() {
		List<Movie> displayList = new ArrayList<>();
		if (isBookmarkSubtabActive) {
			List<Integer> ids = favoritesManager.getFavoriteMovieIds();
			for (int id : ids) {
				Movie m = realtimeManager.getMovieById(id);
				if (m != null) displayList.add(m);
			}
			imgEmptyFavIcon.setImageResource(R.drawable.ic_nav_favorite);
			txtEmptyFavTitle.setText("Belum Ada Film yang Disimpan");
			txtEmptyFavDesc.setText("Tandai film favorit dengan tombol bookmark di halaman detail untuk melihatnya di sini.");
		} else {
			List<Integer> ids = favoritesManager.getWatchHistoryMovieIds();
			for (int id : ids) {
				Movie m = realtimeManager.getMovieById(id);
				if (m != null) displayList.add(m);
			}
			imgEmptyFavIcon.setImageResource(R.drawable.ic_history);
			txtEmptyFavTitle.setText("Belum Ada Riwayat Tonton");
			txtEmptyFavDesc.setText("Film yang Anda buka atau tonton akan otomatis tercatat di halaman riwayat ini.");
		}
		
		favoritesAdapter.updateList(displayList);
		if (displayList.isEmpty()) {
			layoutEmptyFavorites.setVisibility(View.VISIBLE);
			recyclerFavorites.setVisibility(View.GONE);
		} else {
			layoutEmptyFavorites.setVisibility(View.GONE);
			recyclerFavorites.setVisibility(View.VISIBLE);
		}
	}
	
	private void setupDownloadsTab() {
		recyclerTabDownloads.setLayoutManager(new LinearLayoutManager(this));
		tabDownloadAdapter = new DownloadAdapter(this, new ArrayList<>(), new DownloadAdapter.OnDownloadActionListener() {
			@Override
			public void onOpenDownload(DownloadItem item) {
				if (item.getStatus() == DownloadItem.STATUS_SUCCESSFUL) {
					File file = downloadHelper.getDownloadedFile(item);
					if (file != null && file.exists()) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(downloadHelper.getDownloadedUri(item), "video/*");
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						try {
							startActivity(intent);
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, "Tidak ada pemutar video terpasang", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(MainActivity.this, "Berkas unduhan tidak ditemukan di penyimpanan", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(MainActivity.this, "Unduhan masih berlangsung", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onDeleteDownload(DownloadItem item) {
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("Hapus Unduhan")
				.setMessage("Hapus berkas '" + item.getMovieTitle() + "'?")
				.setPositiveButton("Hapus", (dialog, which) -> {
					downloadHelper.deleteDownload(item.getDownloadId());
					refreshDownloadsTab();
					updateDownloadBadge();
				})
				.setNegativeButton("Batal", null)
				.show();
			}
		});
		recyclerTabDownloads.setAdapter(tabDownloadAdapter);
		
		txtTabFolderPath.setText(downloadHelper.getDownloadFolderPath());
	}
	
	private void refreshDownloadsTab() {
		List<DownloadItem> downloads = downloadHelper.getAllDownloads();
		tabDownloadAdapter.updateList(downloads);
		txtTabDownloadsCount.setText(downloads.size() + " Berkas");
		txtTabFolderPath.setText(downloadHelper.getDownloadFolderPath());
		
		if (downloads.isEmpty()) {
			layoutEmptyTabDownloads.setVisibility(View.VISIBLE);
			recyclerTabDownloads.setVisibility(View.GONE);
		} else {
			layoutEmptyTabDownloads.setVisibility(View.GONE);
			recyclerTabDownloads.setVisibility(View.VISIBLE);
		}
		updateDownloadBadge();
	}
	
	private void updateUIWithMovies(List<Movie> movies) {
		if (movies == null || movies.isEmpty()) {
			layoutEmptyFirebase.setVisibility(View.VISIBLE);
			layoutBannerContainer.setVisibility(View.GONE);
			layoutSectionGenres.setVisibility(View.GONE);
			layoutSectionSpotlight.setVisibility(View.GONE);
			layoutSectionTrending.setVisibility(View.GONE);
			layoutSectionTopRated.setVisibility(View.GONE);
			layoutSectionPopular.setVisibility(View.GONE);
			
			txtFirebaseStatus.setText("Disconnected");
			txtFirebaseStatus.setTextColor(Color.parseColor("#94A3B8"));
			dotFirebaseStatus.setBackgroundColor(Color.parseColor("#64748B"));
			txtMovieCount.setText("0 Film");
		} else {
			layoutEmptyFirebase.setVisibility(View.GONE);
			layoutBannerContainer.setVisibility(View.VISIBLE);
			layoutSectionGenres.setVisibility(View.VISIBLE);
			layoutSectionSpotlight.setVisibility(View.VISIBLE);
			layoutSectionTrending.setVisibility(View.VISIBLE);
			layoutSectionTopRated.setVisibility(View.VISIBLE);
			layoutSectionPopular.setVisibility(View.VISIBLE);
			
			txtFirebaseStatus.setText("Connected (" + movies.size() + ")");
			txtFirebaseStatus.setTextColor(Color.parseColor("#10B981"));
			dotFirebaseStatus.setBackgroundColor(Color.parseColor("#10B981"));
			
			updateBannerList(movies);
			
			updateSpotlight(movies);
			
			List<Movie> trending = getTrendingList(movies);
			trendingAdapter.updateList(trending);
			
			List<Movie> topRated = getTopRatedList(movies);
			topRatedAdapter.updateList(topRated);
			if (topRated.isEmpty()) layoutSectionTopRated.setVisibility(View.GONE);
			
			filterMoviesByGenre(selectedGenre);
			
			setupGenreChips();
		}
		
		if (currentTab == TAB_EXPLORE) applyExploreFilters();
		if (currentTab == TAB_FAVORITES) refreshFavoritesTab();
	}
	
	private void updateSpotlight(List<Movie> movies) {
		if (movies.isEmpty()) return;
		Movie best = movies.get(0);
		for (Movie m : movies) {
			if (m.getRating() > best.getRating()) {
				best = m;
			}
		}
		currentSpotlightMovie = best;
		
		txtSpotlightTitle.setText(best.getTitle());
		txtSpotlightRating.setText("★ " + best.getRatingText());
		txtSpotlightMeta.setText((best.getGenre() != null ? best.getGenre() : "Film") + " • " +
		best.getYearText() + " • " + (best.getQuality() != null ? best.getQuality() : "HD"));
		
		Glide.with(this)
		.load(best.getBackdropUrl())
		.placeholder(R.drawable.bg_badge_dark)
		.error(R.drawable.bg_badge_dark)
		.transition(DrawableTransitionOptions.withCrossFade())
		.into(imgSpotlightBackdrop);
	}
	
	private List<Movie> getTrendingList(List<Movie> movies) {
		List<Movie> list = new ArrayList<>(movies);
		Collections.sort(list, (m1, m2) -> {
			if (m1.getTrendingRank() > 0 && m2.getTrendingRank() > 0) {
				return Integer.compare(m1.getTrendingRank(), m2.getTrendingRank());
			} else if (m1.getTrendingRank() > 0) {
				return -1;
			} else if (m2.getTrendingRank() > 0) {
				return 1;
			}
			return Double.compare(m2.getRating(), m1.getRating());
		});
		if (list.size() > 10) {
			return new ArrayList<>(list.subList(0, 10));
		}
		return list;
	}
	
	private List<Movie> getTopRatedList(List<Movie> movies) {
		List<Movie> list = new ArrayList<>();
		for (Movie m : movies) {
			if (m.getRating() >= 8.0) {
				list.add(m);
			}
		}
		Collections.sort(list, (m1, m2) -> Double.compare(m2.getRating(), m1.getRating()));
		return list;
	}
	
	private void setupGenreChips() {
		layoutGenreChips.removeAllViews();
		List<String> list = currentGenresList != null && !currentGenresList.isEmpty() ?
		currentGenresList : realtimeManager.getCurrentGenres();
		
		if (list.isEmpty()) {
			list = new ArrayList<>();
			list.add("Semua");
		}
		
		for (String g : list) {
			TextView chip = new TextView(this);
			chip.setText(g);
			chip.setTextSize(12f);
			chip.setGravity(Gravity.CENTER);
			chip.setPadding(dpToPx(14), dpToPx(7), dpToPx(14), dpToPx(7));
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
			);
			params.setMargins(0, 0, dpToPx(8), 0);
			chip.setLayoutParams(params);
			
			boolean isSelected = g.equalsIgnoreCase(selectedGenre);
			updateChipStyle(chip, isSelected);
			
			chip.setOnClickListener(v -> {
				selectedGenre = g;
				refreshGenreChipsStyle();
				filterMoviesByGenre(g);
			});
			
			layoutGenreChips.addView(chip);
		}
	}
	
	private void updateChipStyle(TextView chip, boolean isSelected) {
		if (isSelected) {
			chip.setBackgroundResource(R.drawable.bg_chip_selected);
			chip.setTextColor(0xFF0B0E14);
			chip.setTypeface(null, Typeface.BOLD);
		} else {
			chip.setBackgroundResource(R.drawable.bg_chip_unselected);
			chip.setTextColor(0xFF94A3B8);
			chip.setTypeface(null, Typeface.NORMAL);
		}
	}
	
	private void refreshGenreChipsStyle() {
		for (int i = 0; i < layoutGenreChips.getChildCount(); i++) {
			TextView chip = (TextView) layoutGenreChips.getChildAt(i);
			if (chip != null) {
				boolean isSelected = chip.getText().toString().equalsIgnoreCase(selectedGenre);
				updateChipStyle(chip, isSelected);
			}
		}
	}
	
	private void updateBannerList(List<Movie> movies) {
		featuredMovies = getFeaturedList(movies);
		bannerAdapter.updateList(featuredMovies);
		setupBannerDots(featuredMovies.size());
	}
	
	private List<Movie> getFeaturedList(List<Movie> movies) {
		if (movies == null || movies.isEmpty()) return new ArrayList<>();
		int count = Math.min(movies.size(), 5);
		return new ArrayList<>(movies.subList(0, count));
	}
	
	private void setupBannerDots(int count) {
		layoutDots.removeAllViews();
		for (int i = 0; i < count; i++) {
			ImageView dot = new ImageView(this);
			dot.setImageDrawable(getDrawable(i == 0 ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
			params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
			dot.setLayoutParams(params);
			layoutDots.addView(dot);
		}
	}
	
	private void updateBannerDots(int activeIndex) {
		for (int i = 0; i < layoutDots.getChildCount(); i++) {
			ImageView dot = (ImageView) layoutDots.getChildAt(i);
			if (dot != null) {
				dot.setImageDrawable(getDrawable(i == activeIndex ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive));
			}
		}
	}
	
	private void startAutoSlide() {
		autoSlideRunnable = () -> {
			if (featuredMovies.size() > 1) {
				int next = (viewPagerBanner.getCurrentItem() + 1) % featuredMovies.size();
				viewPagerBanner.setCurrentItem(next, true);
			}
			autoSlideHandler.postDelayed(autoSlideRunnable, SLIDE_DELAY_MS);
		};
		autoSlideHandler.postDelayed(autoSlideRunnable, SLIDE_DELAY_MS);
	}
	
	private void restartAutoSlide() {
		autoSlideHandler.removeCallbacks(autoSlideRunnable);
		autoSlideHandler.postDelayed(autoSlideRunnable, SLIDE_DELAY_MS);
	}
	
	private void updateDownloadBadge() {
		List<DownloadItem> downloads = downloadHelper.getAllDownloads();
		int activeCount = 0;
		for (DownloadItem item : downloads) {
			if (item.getStatus() == DownloadItem.STATUS_RUNNING || item.getStatus() == DownloadItem.STATUS_PENDING) {
				activeCount++;
			}
		}
		if (activeCount > 0) {
			txtNavDownloadBadge.setVisibility(View.VISIBLE);
			txtNavDownloadBadge.setText(String.valueOf(activeCount));
		} else {
			txtNavDownloadBadge.setVisibility(View.GONE);
		}
	}
	
	private void openMovieDetail(Movie movie) {
		Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
		intent.putExtra("movie_id", movie.getId());
		startActivity(intent);
	}
	
	private int dpToPx(int dp) {
		return Math.round(dp * getResources().getDisplayMetrics().density);
	}
	
	@Override
	public void onDataChanged(List<Movie> movies, List<String> genresList) {
		this.currentGenresList = genresList;
		updateUIWithMovies(movies);
	}
	
	@Override
	public void onUserDataChanged() {
		if (currentTab == TAB_FAVORITES) {
			refreshFavoritesTab();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		restartAutoSlide();
		updateDownloadBadge();
		if (currentTab == TAB_FAVORITES) refreshFavoritesTab();
		if (currentTab == TAB_DOWNLOADS) refreshDownloadsTab();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		autoSlideHandler.removeCallbacks(autoSlideRunnable);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		realtimeManager.removeListener(this);
		favoritesManager.removeListener(this);
	}
}