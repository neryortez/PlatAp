package io.github.rathn.platap.utils;

import android.content.Context;

//import com.example.neriortez.dolarpajaro.R;
import io.github.rathn.platap.R;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.persistent.PersistentStorage;

public class CategoryUtils {

    public static int[] sCategoryColorIndexes = new int[]{3, 4, 36, 37, 22, 23, 38, 39, 40, 41, 42, 0, 13, 43, 44, 12, 14, 2, 30, 1, 31, 32, 45, 6, 46, 47, 48, 49, 7, 8, 50, 24, 26, 25, 51, 52, 20, 18, 19, 53, 54, 55, 56, 57, 58, 59, 60, 61, 9, 62, 11, 63, 10, 5, 64, 21, 65, 16, 66, 17, 67, 15, 68, 27, 28, 29, 69, 70, 71, 72, 73, 74, 34, 35, 75, 33, 76, 77, 78, 79, 80};
    public static int[] sCategoryColors = new int[]{R.color.categoryRed3, 
            R.color.categoryMagenta2, R.color.categoryRed9, R.color.categoryYellow1, 
            R.color.categoryYellow2, R.color.categoryGreen9, R.color.categoryMagenta6, 
            R.color.categoryPurple2, R.color.categoryPurple3, R.color.categoryGreen4, 
            R.color.categoryGreen8, R.color.categoryGreen6, R.color.categoryRed7, 
            R.color.categoryRed4, R.color.categoryRed8, R.color.categoryBrown8, 
            R.color.categoryBrown4, R.color.categoryBrown6, R.color.categoryBlue2, 
            R.color.categoryBlue3, R.color.categoryBlue1, R.color.categoryBrown2, 
            R.color.categoryOrange2, R.color.categoryOrange3, R.color.categoryPurple5, 
            R.color.categoryPurple7, R.color.categoryPurple6, R.color.categoryGrey1, 
            R.color.categoryGrey2, R.color.categoryGrey3, R.color.categoryMagenta1, 
            R.color.categoryMagenta3, R.color.categoryMagenta4, R.color.categoryGreyDark4, 
            R.color.categoryGreyDark1, R.color.categoryGreyDark2, R.color.categoryYellow3, R.color.categoryOrange1, R.color.categoryOrange4, R.color.categoryOrange5, R.color.categoryOrange6, R.color.categoryRed1, R.color.categoryRed2, R.color.categoryRed5, R.color.categoryRed6, R.color.categoryMagenta5, R.color.categoryMagenta7, R.color.categoryMagenta8, R.color.categoryMagenta9, R.color.categoryPurple1, R.color.categoryPurple4, R.color.categoryPurple8, R.color.categoryPurple9, R.color.categoryBlue4, R.color.categoryBlue5, R.color.categoryBlue6, R.color.categoryBlue7, R.color.categoryBlue8, R.color.categoryBlue9, R.color.categoryGreen1, R.color.categoryGreen2, R.color.categoryGreen3, R.color.categoryGreen5, R.color.categoryGreen7, R.color.categoryBrown1, R.color.categoryBrown3, R.color.categoryBrown5, R.color.categoryBrown7, R.color.categoryBrown9, R.color.categoryGrey4, R.color.categoryGrey5, R.color.categoryGrey6, R.color.categoryGrey7, R.color.categoryGrey8, R.color.categoryGrey9, R.color.categoryGreyDark3, R.color.categoryGreyDark5, R.color.categoryGreyDark6, R.color.categoryGreyDark7, R.color.categoryGreyDark8, R.color.categoryGreyDark9};
    public static int[] sCategoryGrayIcons = new int[]{R.drawable.ic_category_other_gray, R.drawable.ic_category_clothing_gray, R.drawable.ic_category_eating_out_gray, R.drawable.ic_category_education_gray, R.drawable.ic_category_fun_gray, R.drawable.ic_category_gifts_gray, R.drawable.ic_category_groceries_gray, R.drawable.ic_category_medical_gray, R.drawable.ic_category_household_gray, R.drawable.ic_category_rent_loan_gray, R.drawable.ic_category_salary_gray, R.drawable.ic_category_car_gray, R.drawable.ic_category_transport_gray, R.drawable.ic_category_travel_gray, R.drawable.ic_category_utilities_gray};
    public static int[] sCategoryWhiteIcons = new int[]{R.drawable.ic_category_other_white, R.drawable.ic_category_clothing_white, R.drawable.ic_category_eating_out_white, R.drawable.ic_category_education_white, R.drawable.ic_category_fun_white, R.drawable.ic_category_gifts_white, R.drawable.ic_category_groceries_white, R.drawable.ic_category_medical_white, R.drawable.ic_category_household_white, R.drawable.ic_category_rent_loan_white, R.drawable.ic_category_salary_white, R.drawable.ic_category_car_white, R.drawable.ic_category_transport_white, R.drawable.ic_category_travel_white, R.drawable.ic_category_utilities_white};

    public static int[] sPalettColor = new int[]{R.color.categoryYellow1, R.color.categoryOrange1,
            R.color.categoryOrange4, R.color.categoryRed1,
            R.color.categoryRed5, R.color.categoryRed9, R.color.categoryMagenta4,
            R.color.categoryMagenta8, R.color.categoryPurple3, R.color.categoryPurple7,
            R.color.categoryBlue2, R.color.categoryBlue6, R.color.categoryGreen1,
            R.color.categoryGreen5, R.color.categoryGreen9, R.color.categoryBrown4,
            R.color.categoryBrown8, R.color.categoryGrey3,R.color.categoryGrey7,
            R.color.categoryGreyDark2, R.color.categoryGreyDark6, };

    public static int[] sPalettColors = new int[]{R.color.categoryYellow1,
            R.color.categoryYellow2, R.color.categoryYellow3, R.color.categoryOrange1, 
            R.color.categoryOrange2, R.color.categoryOrange3, R.color.categoryOrange4, 
            R.color.categoryOrange5, R.color.categoryOrange6, R.color.categoryRed1, 
            R.color.categoryRed2, R.color.categoryRed3, R.color.categoryRed4, 
            R.color.categoryRed5, R.color.categoryRed6, R.color.categoryRed7, 
            R.color.categoryRed8, R.color.categoryRed9, R.color.categoryMagenta1, 
            R.color.categoryMagenta2, R.color.categoryMagenta3, R.color.categoryMagenta4, 
            R.color.categoryMagenta5, R.color.categoryMagenta6, R.color.categoryMagenta7, 
            R.color.categoryMagenta8, R.color.categoryMagenta9, R.color.categoryPurple1, 
            R.color.categoryPurple2, R.color.categoryPurple3, R.color.categoryPurple4, 
            R.color.categoryPurple5, R.color.categoryPurple6, R.color.categoryPurple7, 
            R.color.categoryPurple8, R.color.categoryPurple9, R.color.categoryBlue1, 
            R.color.categoryBlue2, R.color.categoryBlue3, R.color.categoryBlue4, 
            R.color.categoryBlue5, R.color.categoryBlue6, R.color.categoryBlue7, 
            R.color.categoryBlue8, R.color.categoryBlue9, R.color.categoryGreen1, 
            R.color.categoryGreen2, R.color.categoryGreen3, R.color.categoryGreen4, 
            R.color.categoryGreen5, R.color.categoryGreen6, R.color.categoryGreen7, 
            R.color.categoryGreen8, R.color.categoryGreen9, R.color.categoryBrown1, 
            R.color.categoryBrown2, R.color.categoryBrown3, R.color.categoryBrown4, 
            R.color.categoryBrown5, R.color.categoryBrown6, R.color.categoryBrown7, 
            R.color.categoryBrown8, R.color.categoryBrown9, R.color.categoryGrey1, 
            R.color.categoryGrey2, R.color.categoryGrey3, R.color.categoryGrey4, 
            R.color.categoryGrey5, R.color.categoryGrey6, R.color.categoryGrey7, 
            R.color.categoryGrey8, R.color.categoryGrey9, R.color.categoryGreyDark1, 
            R.color.categoryGreyDark2, R.color.categoryGreyDark3, R.color.categoryGreyDark4, 
            R.color.categoryGreyDark5, R.color.categoryGreyDark6, R.color.categoryGreyDark7, 
            R.color.categoryGreyDark8, R.color.categoryGreyDark9};
    public static int[] sProCategoryGrayIcons = new int[]{R.drawable.ic_category_pro_shoppingcar_gray, R.drawable.ic_category_pro_milk_gray, R.drawable.ic_category_pro_water_gray, R.drawable.ic_category_pro_gas_gray, R.drawable.ic_category_pro_credit_card_gray, R.drawable.ic_category_pro_pigggy_bank_gray, R.drawable.ic_category_pro_tickets_gray, R.drawable.ic_category_pro_app_store_gray, R.drawable.ic_category_pro_iphone_gray, R.drawable.ic_category_pro_android_phone_gray, R.drawable.ic_category_pro_photography_gray, R.drawable.ic_category_pro_headphones_gray, R.drawable.ic_category_pro_monitor_gray, R.drawable.ic_category_pro_mouse_gray, R.drawable.ic_category_pro_camera_gray, R.drawable.ic_category_pro_calculator_gray, R.drawable.ic_category_pro_bike_gray, R.drawable.ic_category_pro_motorcycle_gray, R.drawable.ic_category_pro_taxi_gray, R.drawable.ic_category_pro_vespa_gray, R.drawable.ic_category_pro_sailboat_gray, R.drawable.ic_category_pro_coffee_gray, R.drawable.ic_category_pro_tea_gray, R.drawable.ic_category_pro_wine_gray, R.drawable.ic_category_pro_beer_gray, R.drawable.ic_category_pro_drinks_gray, R.drawable.ic_category_pro_pear_gray, R.drawable.ic_category_pro_pineapple_gray, R.drawable.ic_category_pro_burger_gray, R.drawable.ic_category_pro_pizza_gray, R.drawable.ic_category_pro_bowl_gray, R.drawable.ic_category_pro_cupcake_gray, R.drawable.ic_category_pro_cooking_gray, R.drawable.ic_category_pro_fitness_gray, R.drawable.ic_category_pro_basketball_gray, R.drawable.ic_category_pro_football_gray, R.drawable.ic_category_pro_golf_car_gray, R.drawable.ic_category_pro_skateboard_gray, R.drawable.ic_category_pro_jacket_gray, R.drawable.ic_category_pro_jeans_gray, R.drawable.ic_category_pro_shoe_gray, R.drawable.ic_category_pro_tshirt_women_gray, R.drawable.ic_category_pro_dress_gray, R.drawable.ic_category_pro_highheel_gray, R.drawable.ic_category_pro_boots_gray, R.drawable.ic_category_pro_bag_gray, R.drawable.ic_category_pro_suitcase_gray, R.drawable.ic_category_pro_hat_gray, R.drawable.ic_category_pro_hat2_gray, R.drawable.ic_category_pro_watch_gray, R.drawable.ic_category_pro_glasses_gray, R.drawable.ic_category_pro_sunglasses_gray, R.drawable.ic_category_pro_bow_tie_gray, R.drawable.ic_category_pro_ballet_gray, R.drawable.ic_category_pro_ski_glass_gray, R.drawable.ic_category_pro_book_gray, R.drawable.ic_category_pro_paintbrush_gray, R.drawable.ic_category_pro_marker_gray, R.drawable.ic_category_pro_pen_gray, R.drawable.ic_category_pro_game_gray, R.drawable.ic_category_pro_guitar_gray, R.drawable.ic_category_pro_speakers_gray, R.drawable.ic_category_pro_billiard_gray, R.drawable.ic_category_pro_gambling_gray, R.drawable.ic_category_pro_fishing_gray, R.drawable.ic_category_pro_camping_gray, R.drawable.ic_category_pro_holiday_gray, R.drawable.ic_category_pro_hotel_gray, R.drawable.ic_category_pro_trip_gray, R.drawable.ic_category_pro_camping2_gray, R.drawable.ic_category_pro_diamond_gray, R.drawable.ic_category_pro_hat3_gray, R.drawable.ic_category_pro_birthday_gray, R.drawable.ic_category_pro_christmas_gray, R.drawable.ic_category_pro_easter_gray, R.drawable.ic_category_pro_thanksgiving_gray, R.drawable.ic_category_pro_wedding_gray, R.drawable.ic_category_pro_baby_gray, R.drawable.ic_category_pro_dog_gray, R.drawable.ic_category_pro_stroller_gray, R.drawable.ic_category_pro_baby_bottle_gray, R.drawable.ic_category_pro_duck_gray, R.drawable.ic_category_pro_rocking_horse_gray, R.drawable.ic_category_pro_chair_gray, R.drawable.ic_category_pro_lamp_gray, R.drawable.ic_category_pro_plant_gray, R.drawable.ic_category_pro_washing_machine_gray, R.drawable.ic_category_pro_tv_gray, R.drawable.ic_category_pro_typewriter_gray, R.drawable.ic_category_pro_tools_gray, R.drawable.ic_category_pro_toilet_paper_gray, R.drawable.ic_category_pro_paint_roller_gray, R.drawable.ic_category_pro_cleaning_gray, R.drawable.ic_category_pro_scissors_gray, R.drawable.ic_category_pro_sewing_machine_gray, R.drawable.ic_category_pro_button_gray, R.drawable.ic_category_pro_scale_gray, R.drawable.ic_category_pro_lips_gray, R.drawable.ic_category_pro_lipstick_gray, R.drawable.ic_category_pro_medicine_gray, R.drawable.ic_category_pro_perfume_gray};
    public static int[] sProCategoryWhiteIcons = new int[]{R.drawable.ic_category_pro_shoppingcar_white, R.drawable.ic_category_pro_milk_white, R.drawable.ic_category_pro_water_white, R.drawable.ic_category_pro_gas_white, R.drawable.ic_category_pro_credit_card_white, R.drawable.ic_category_pro_pigggy_bank_white, R.drawable.ic_category_pro_tickets_white, R.drawable.ic_category_pro_app_store_white, R.drawable.ic_category_pro_iphone_white, R.drawable.ic_category_pro_android_phone_white, R.drawable.ic_category_pro_photography_white, R.drawable.ic_category_pro_headphones_white, R.drawable.ic_category_pro_monitor_white, R.drawable.ic_category_pro_mouse_white, R.drawable.ic_category_pro_camera_white, R.drawable.ic_category_pro_calculator_white, R.drawable.ic_category_pro_bike_white, R.drawable.ic_category_pro_motorcycle_white, R.drawable.ic_category_pro_taxi_white, R.drawable.ic_category_pro_vespa_white, R.drawable.ic_category_pro_sailboat_white, R.drawable.ic_category_pro_coffee_white, R.drawable.ic_category_pro_tea_white, R.drawable.ic_category_pro_wine_white, R.drawable.ic_category_pro_beer_white, R.drawable.ic_category_pro_drinks_white, R.drawable.ic_category_pro_pear_white, R.drawable.ic_category_pro_pineapple_white, R.drawable.ic_category_pro_burger_white, R.drawable.ic_category_pro_pizza_white, R.drawable.ic_category_pro_bowl_white, R.drawable.ic_category_pro_cupcake_white, R.drawable.ic_category_pro_cooking_white, R.drawable.ic_category_pro_fitness_white, R.drawable.ic_category_pro_basketball_white, R.drawable.ic_category_pro_football_white, R.drawable.ic_category_pro_golf_car_white, R.drawable.ic_category_pro_skateboard_white, R.drawable.ic_category_pro_jacket_white, R.drawable.ic_category_pro_jeans_white, R.drawable.ic_category_pro_shoe_white, R.drawable.ic_category_pro_tshirt_women_white, R.drawable.ic_category_pro_dress_white, R.drawable.ic_category_pro_highheel_white, R.drawable.ic_category_pro_boots_white, R.drawable.ic_category_pro_bag_white, R.drawable.ic_category_pro_suitcase_white, R.drawable.ic_category_pro_hat_white, R.drawable.ic_category_pro_hat2_white, R.drawable.ic_category_pro_watch_white, R.drawable.ic_category_pro_glasses_white, R.drawable.ic_category_pro_sunglasses_white, R.drawable.ic_category_pro_bow_tie_white, R.drawable.ic_category_pro_ballet_white, R.drawable.ic_category_pro_ski_glass_white, R.drawable.ic_category_pro_book_white, R.drawable.ic_category_pro_paintbrush_white, R.drawable.ic_category_pro_marker_white, R.drawable.ic_category_pro_pen_white, R.drawable.ic_category_pro_game_white, R.drawable.ic_category_pro_guitar_white, R.drawable.ic_category_pro_speakers_white, R.drawable.ic_category_pro_billiard_white, R.drawable.ic_category_pro_gambling_white, R.drawable.ic_category_pro_fishing_white, R.drawable.ic_category_pro_camping_white, R.drawable.ic_category_pro_holiday_white, R.drawable.ic_category_pro_hotel_white, R.drawable.ic_category_pro_trip_white, R.drawable.ic_category_pro_camping2_white, R.drawable.ic_category_pro_diamond_white, R.drawable.ic_category_pro_hat3_white, R.drawable.ic_category_pro_birthday_white, R.drawable.ic_category_pro_christmas_white, R.drawable.ic_category_pro_easter_white, R.drawable.ic_category_pro_thanksgiving_white, R.drawable.ic_category_pro_wedding_white, R.drawable.ic_category_pro_baby_white, R.drawable.ic_category_pro_dog_white, R.drawable.ic_category_pro_stroller_white, R.drawable.ic_category_pro_baby_bottle_white, R.drawable.ic_category_pro_duck_white, R.drawable.ic_category_pro_rocking_horse_white, R.drawable.ic_category_pro_chair_white, R.drawable.ic_category_pro_lamp_white, R.drawable.ic_category_pro_plant_white, R.drawable.ic_category_pro_washing_machine_white, R.drawable.ic_category_pro_tv_white, R.drawable.ic_category_pro_typewriter_white, R.drawable.ic_category_pro_tools_white, R.drawable.ic_category_pro_toilet_paper_white, R.drawable.ic_category_pro_paint_roller_white, R.drawable.ic_category_pro_cleaning_white, R.drawable.ic_category_pro_scissors_white, R.drawable.ic_category_pro_sewing_machine_white, R.drawable.ic_category_pro_button_white, R.drawable.ic_category_pro_scale_white, R.drawable.ic_category_pro_lips_white, R.drawable.ic_category_pro_lipstick_white, R.drawable.ic_category_pro_medicine_white, R.drawable.ic_category_pro_perfume_white};

    public static int[] sAllCategoryGrayIcons = new int[(sCategoryGrayIcons.length + sProCategoryGrayIcons.length)];
    public static int[] sAllCategoryWhiteIcons = new int[(sCategoryWhiteIcons.length + sProCategoryWhiteIcons.length)];



    public static Category getAddNewCategory(boolean isExpense) {
        Category category = new Category();
        category.setIsExpense(isExpense);
        return category;
    }

    public static int getWhiteIconResourceIdForIndex(int index) {
        if (PersistentStorage.isFreeVersionRunning()) {
            if (index == -1) {
                return R.drawable.ic_category_add_new;
            }
            if (index < sCategoryWhiteIcons.length) {
                return sCategoryWhiteIcons[index];
            }
            return sCategoryWhiteIcons[0];
        } else if (index == -1) {
            return R.drawable.ic_category_add_new;
        } else {
            if (index < sAllCategoryWhiteIcons.length) {
                return sAllCategoryWhiteIcons[index];
            }
            return sAllCategoryWhiteIcons[0];
        }
    }

    public static int getGrayIconResourceIdForIndex(int index) {
        if (PersistentStorage.isFreeVersionRunning()) {
            if (index == -1) {
                return R.drawable.ic_category_add_new;
            }
            if (index < sCategoryGrayIcons.length) {
                return sCategoryGrayIcons[index];
            }
            return sCategoryGrayIcons[0];
        } else if (index == -1) {
            return R.drawable.ic_category_add_new;
        } else {
            if (index < sAllCategoryGrayIcons.length) {
                return sAllCategoryGrayIcons[index];
            }
            return sAllCategoryGrayIcons[0];
        }
    }

    public static int getColorsResourceIdForIndex(int index, Context context) {
        if (index == -1) {
            return R.color.category_add_new;
        }
        /*if (index < sCategoryColors.length) {
            return sCategoryColors[index];
        }*/if (index < sPalettColor.length){
            return sPalettColor[index];
        }
        return sPalettColor[0];
    }

    static {
        System.arraycopy(sCategoryWhiteIcons, 0, sAllCategoryWhiteIcons, 0, sCategoryWhiteIcons.length);
        System.arraycopy(sProCategoryWhiteIcons, 0, sAllCategoryWhiteIcons, sCategoryWhiteIcons.length, sProCategoryWhiteIcons.length);
        System.arraycopy(sCategoryGrayIcons, 0, sAllCategoryGrayIcons, 0, sCategoryGrayIcons.length);
        System.arraycopy(sProCategoryGrayIcons, 0, sAllCategoryGrayIcons, sCategoryGrayIcons.length, sProCategoryGrayIcons.length);
    }
}
