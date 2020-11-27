package com.shantanoo.news_gateway.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    public static final String ARTICLE = "ARTICLE";
    public static final String INDEX = "INDEX";
    public static final String TOTAL = "TOTAL";
    public static final String NOT_FOUND = "";
    public static final String DATE_PATTERN = "MMM dd, yyyy HH:mm";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
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
        articleCount.setText(new StringBuilder().append(getArguments().getInt(INDEX) + 1).append(" of ").append(getArguments().getInt(TOTAL)).toString());

        article = (NewsArticle) getArguments().getSerializable(ARTICLE);
        // Update article title only if it is not null and "null"
        if (article.getTitle() != null && !article.getTitle().trim().equals("null"))
            articleHeadLine.setText(article.getTitle());
        else
            articleHeadLine.setText(NOT_FOUND);

        // Update article published date only if it is not null and "null"
        if (article.getPublishedAt() != null && !article.getPublishedAt().isEmpty() && !article.getPublishedAt().trim().equals("null")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
                articleDate.setText(sdf.format(new SimpleDateFormat(DATE_FORMAT).parse(article.getPublishedAt())));
            } catch (ParseException e) {

            }
        }

        // Update article author only if it is not null and "null"
        if (article.getAuthor() != null && !article.getAuthor().trim().equals("null"))
            articleAuthor.setText(article.getAuthor());
        else
            articleAuthor.setText(NOT_FOUND);

        // Update article text only if it is not null and "null"
        if (article.getDescription() != null && !article.getDescription().trim().equals("null"))
            articleText.setText(article.getDescription());
        else
            articleText.setText(NOT_FOUND);

        // Update article image URL only if it is not null and "null"
        if (article.getUrlToImage() != null) {
            updateImage(article.getUrlToImage(), !article.getUrlToImage().trim().equals("null"));
        }

        // Make article head line clickable and nav to article link
        articleHeadLine.setOnClickListener(v -> startIntent());

        // Make article photo clickable and nav to article link
        articlePhoto.setOnClickListener(v -> startIntent());

        // Make article text clickable and nav to article link
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

    // Update the image in the image view
    private void updateImage(final String imageURL, boolean displayImage) {
        if (displayImage) {
            Log.d(TAG, "image URL : " + imageURL);
            Picasso picasso = new Picasso.Builder(getActivity()).listener((picasso1, uri, exception) -> exception.printStackTrace()).build();
            // Enable logging to check for errors
            picasso.setLoggingEnabled(true);
            // Load the image, if any error then broken image is loaded.
            picasso.load(imageURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .into(articlePhoto);
        } else {
            Log.d(TAG, "updateImage imageURL is  String(null) actual is -> : " + imageURL);
            articlePhoto.setImageResource(R.drawable.missing);
        }
        Log.d(TAG, "updateImage: COMPLETED");
    }
}