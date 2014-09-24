package fm.feed.android.testapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import fm.feed.android.testapp.R;

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
public class SlidingBottomFragment extends Fragment {
    private static final String TAG = SlidingBottomFragment.class.getSimpleName();

    private View mOverlay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_sliding_bottom, container, false);

        mOverlay = rootView.findViewById(R.id.overlay);

        rootView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            mOverlay.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out));
        } else {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);

            anim.setAnimationListener(new Animation.AnimationListener() {

                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "Animation started.");
                    // additional functionality

                    mOverlay.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in));
                }

                public void onAnimationRepeat(Animation animation) {
                    Log.d(TAG, "Animation repeating.");
                    // additional functionality
                }

                public void onAnimationEnd(Animation animation) {
                    Log.d(TAG, "Animation ended.");
                    // additional functionality
                }
            });
            return anim;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);

    }
}
