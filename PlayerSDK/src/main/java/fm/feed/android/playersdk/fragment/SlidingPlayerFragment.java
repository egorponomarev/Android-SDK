package fm.feed.android.playersdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;

import fm.feed.android.playersdk.util.UIUtils;
import fm.feed.android.playersdk.view.PlayerView;
import fm.feed.android.playersdk.R;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 9/23/14.
 */
public class SlidingPlayerFragment extends Fragment {
    private static final int DEFAULT_SVG_SIZE_DP = 26;

    @Override
    public void onStart() {
        super.onStart();

        // Hide the Action Bar
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.feed_fm_sliding_player_fragment, container, false);

        /**
         * Override default PlayerView Shared Subject title
         */
        PlayerView playerView = (PlayerView) rootView.findViewById(R.id.player);
        playerView.setShareSubject("Currently listening from a sliding panel!");

        int margin = (int) UIUtils.convertDpToPixel(getActivity(), getResources().getDimension(R.dimen.player_padding));
        int sizeBaseline = (int) UIUtils.convertDpToPixel(getActivity(), DEFAULT_SVG_SIZE_DP) + margin * 2;

        SVGImageView closeButton = new SVGImageView(getActivity());

        try {
            closeButton.setSVG(SVG.getFromResource(getActivity(), R.drawable.ic_backarrow_normal));
            closeButton.setContentDescription(getString(R.string.accessibility_back));
        } catch (SVGParseException e) {
            e.printStackTrace();
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(sizeBaseline, sizeBaseline);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        closeButton.setLayoutParams(layoutParams);
        closeButton.setScaleType(ImageView.ScaleType.FIT_START);
        int padding = sizeBaseline / 4;
        closeButton.setPadding(margin, margin, margin, margin);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        playerView.addView(closeButton);


        return rootView;
    }
}
