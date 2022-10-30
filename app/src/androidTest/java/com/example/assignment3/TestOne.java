package com.example.assignment3;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestOne {

    @Rule
    public ActivityScenarioRule<MapsActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapsActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Test
    public void testOne() {
        ViewInteraction editText = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_search_input),
                        childAtPosition(
                                allOf(withId(R.id.search),
                                        childAtPosition(
                                                withId(R.id.card_view),
                                                0)),
                                1),
                        isDisplayed()));
        editText.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_search_bar),
                        childAtPosition(
                                allOf(withId(androidx.appcompat.R.id.places_autocomplete_search_bar_container),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("hamil"), closeSoftKeyboard());

        ViewInteraction recyclerView = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction editText2 = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_search_input), withText("Hamilton"),
                        childAtPosition(
                                allOf(withId(R.id.search),
                                        childAtPosition(
                                                withId(R.id.card_view),
                                                0)),
                                1),
                        isDisplayed()));
        editText2.perform(longClick());

        ViewInteraction editText3 = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_search_input), withText("Hamilton"),
                        childAtPosition(
                                allOf(withId(R.id.search),
                                        childAtPosition(
                                                withId(R.id.card_view),
                                                0)),
                                1),
                        isDisplayed()));
        editText3.perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView2.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction editText4 = onView(
                allOf(withId(androidx.appcompat.R.id.places_autocomplete_search_input), withText("Hamilton"),
                        withParent(allOf(withId(R.id.search),
                                withParent(withId(R.id.card_view)))),
                        isDisplayed()));
        editText4.check(matches(withText("Hamilton")));

        ViewInteraction view = onView(
                allOf(withContentDescription("Cloud. overcast clouds, 16.19°C, Humidity: 99, Wind Speed: 1.68."),
                        withParent(allOf(withContentDescription("Google Map"),
                                withParent(IsInstanceOf.<View>instanceOf(FrameLayout.class)))),
                        isDisplayed()));
        view.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.camTv), withText("Hamilton › South: Te Rapa Rd/Wairere Dr Intersection Hamilton, Waikato"),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        textView.check(matches(withText("Hamilton › South: Te Rapa Rd/Wairere Dr Intersection Hamilton, Waikato")));

        ViewInteraction image = onView(
                allOf(withText("1475827885"),
                        withParent(allOf(withText("1475827885.jpg (400×224)"),
                                withParent(IsInstanceOf.<View>instanceOf(WebView.class)))),
                        isDisplayed()));
        image.check(matches(isDisplayed()));

        pressBack();
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
