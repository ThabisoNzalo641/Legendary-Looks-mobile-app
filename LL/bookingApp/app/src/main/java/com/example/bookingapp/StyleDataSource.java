package com.example.bookingapp;

import java.util.ArrayList;
import java.util.List;

public class StyleDataSource {

    public static List<StyleItem> getHairStyles() {
        List<StyleItem> hairStyles = new ArrayList<>();

        // Temporary sample data - this will be replaced by Firebase
        hairStyles.add(new StyleItem("Haircut", "Professional haircut", "hair",
                "", 25.00, 30));
        hairStyles.add(new StyleItem("Hair Coloring", "Professional hair coloring", "hair",
                "", 60.00, 90));
        hairStyles.add(new StyleItem("Hair Styling", "Special occasion styling", "hair",
                "", 35.00, 45));
        hairStyles.add(new StyleItem("Hair Treatment", "Deep conditioning treatment", "hair",
                "", 40.00, 60));

        return hairStyles;
    }

    // Add the missing getLashStyles method
    public static List<StyleItem> getLashStyles() {
        List<StyleItem> lashStyles = new ArrayList<>();

        lashStyles.add(new StyleItem("Classic Lashes", "Natural lash extensions", "lashes",
                "", 45.00, 60));
        lashStyles.add(new StyleItem("Volume Lashes", "Full volume lash set", "lashes",
                "", 65.00, 90));
        lashStyles.add(new StyleItem("Hybrid Lashes", "Mix of classic and volume", "lashes",
                "", 55.00, 75));
        lashStyles.add(new StyleItem("Lash Lift", "Lash lift and tint", "lashes",
                "", 35.00, 45));

        return lashStyles;
    }

    // You can add other categories if needed
    public static List<StyleItem> getNailStyles() {
        List<StyleItem> nailStyles = new ArrayList<>();
        nailStyles.add(new StyleItem("Manicure", "Basic manicure", "nails",
                "", 25.00, 45));
        nailStyles.add(new StyleItem("Pedicure", "Relaxing pedicure", "nails",
                "", 35.00, 60));
        return nailStyles;
    }

    // Optional: Add brow styles if needed
    public static List<StyleItem> getBrowStyles() {
        List<StyleItem> browStyles = new ArrayList<>();
        browStyles.add(new StyleItem("Brow Shaping", "Professional brow shaping", "brows",
                "", 20.00, 30));
        browStyles.add(new StyleItem("Brow Tinting", "Brow tint and shape", "brows",
                "", 25.00, 45));
        return browStyles;
    }
}