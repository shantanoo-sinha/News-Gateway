package com.shantanoo.news_gateway.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.shantanoo.news_gateway.R;
import com.shantanoo.news_gateway.model.NewsArticle;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    public static final String ARTICLE = "ARTICLE";
    public static final String INDEX = "INDEX";
    public static final String TOTAL = "TOTAL";
    public static final String DATE_FORMAT = "MMM dd, yyyy HH:mm";
    public static final String DATE_FORMAT_PARSE = "yyyy-MM-dd'T'HH:mm:ss";
    private static final SimpleDateFormat sdfFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final SimpleDateFormat sdfParse = new SimpleDateFormat(DATE_FORMAT_PARSE);
    private static final String TAG = "NewsFragment";
    private TextView articleHeadLine;
    private TextView articleDate;
    private TextView articleAuthor;
    private TextView articleText;
    private ImageView articlePhoto;
    private TextView articleCount;
    private NewsArticle article;
    private View view;

    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param article Parameter 1.
     * @param index   Parameter 2.
     * @param total   Parameter 3.
     * @return A new instance of fragment NewsFragment.
     */
    public static NewsFragment newInstance(NewsArticle article, int index, int total) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(ARTICLE, article);
        args.putInt(INDEX, index);
        args.putInt(TOTAL, total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_news, container, false);

        articleHeadLine = view.findViewById(R.id.articleHeadline);
        articleDate = view.findViewById(R.id.articleDate);
        articleAuthor = view.findViewById(R.id.articleAuthor);
        articleText = view.findViewById(R.id.articleText);
        articlePhoto = view.findViewById(R.id.articleImage);
        articleCount = view.findViewById(R.id.articleCount);

        article = (NewsArticle) getArguments().getSerializable(ARTICLE);

        // Show article title only if it is not null and not equal to "null"
        if (isNull(article.getTitle()))
            articleHeadLine.setVisibility(View.GONE);
        else
            articleHeadLine.setText(article.getTitle());

        // Show article published date only if it is not null and not equal to "null"
        if (!isNull(article.getPublishedAt())) {
            try {
                Date parsedDate = sdfParse.parse(article.getPublishedAt());
                if (parsedDate != null) {
                    articleDate.setText(sdfFormat.format(parsedDate));
                }
            } catch (ParseException e) {
                Log.e(TAG, "onCreateView: Failed to parse date", e);
            }
        }

        // Show article author only if it is not null and not equal to "null"
        if (isNull(article.getAuthor()))
            articleAuthor.setVisibility(View.GONE);
        else
            articleAuthor.setText(article.getAuthor());

        // Show article text only if it is not null and not equal to "null"
        if (isNull(article.getDescription()))
            articleText.setVisibility(View.GONE);
        else
            articleText.setText(article.getDescription());

        // Show article image only if URL is not null and not equal to "null"
        if (isNull(article.getUrlToImage()))
            articlePhoto.setVisibility(View.GONE);
        else
            showImage(article.getUrlToImage());

        articleCount.setText(String.format("%d of %d", getArguments().getInt(INDEX) + 1, getArguments().getInt(TOTAL)));

        // Article headline clickable and navigate to article in browser
        articleHeadLine.setOnClickListener(v -> startIntent());

        // Article photo clickable and navigate to article in browser
        articlePhoto.setOnClickListener(v -> startIntent());

        // Article text clickable and navigate to article in browser
        articleText.setOnClickListener(v -> startIntent());

        return view;
    }

    private void startIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(article.getUrl()));
        startActivity(intent);
    }

    // Show the image in the image view
    private void showImage(final String imageURL) {
        Log.d(TAG, "image URL : " + imageURL);
        Picasso picasso = new Picasso.Builder(getActivity()).listener((picasso1, uri, exception) -> exception.printStackTrace()).build();
        // Enable logging to check for errors
        picasso.setLoggingEnabled(true);
        // Load the image, if any error then broken image is loaded.
        picasso.load(imageURL)
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.placeholder)
                .into(articlePhoto);
    }

    private boolean isNull(String input) {
        return TextUtils.isEmpty(input) || input.trim().equals("null");
    }
}