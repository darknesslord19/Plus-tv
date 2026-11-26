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

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class Parallax3DTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        float width = page.getWidth();

        if (position < -1) {
            page.setAlpha(0.4f);
            page.setScaleX(0.88f);
            page.setScaleY(0.88f);
            page.setRotationY(-20f);
        } else if (position <= 1) {
            float scaleFactor = Math.max(0.88f, 1 - Math.abs(position) * 0.12f);
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            page.setCameraDistance(width * 15f);
            float rotation = position * -22f;
            page.setRotationY(rotation);

            float alpha = Math.max(0.6f, 1 - Math.abs(position) * 0.4f);
            page.setAlpha(alpha);

            View innerBackdrop = page.findViewById(R.id.img_banner_backdrop);
            if (innerBackdrop != null) {
                innerBackdrop.setTranslationX(-position * (width / 3f));
            }
        } else {
            page.setAlpha(0.4f);
            page.setScaleX(0.88f);
            page.setScaleY(0.88f);
            page.setRotationY(20f);
        }
    }
}