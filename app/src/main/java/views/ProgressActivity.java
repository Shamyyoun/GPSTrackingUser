package views;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.mahmoudelshamy.gpstracking.userapp.R;

import utils.ViewUtil;

public abstract class ProgressActivity extends ActionBarActivity {
    // constants for view states
    private static final int VIEW_STATE_MAIN = 1;
    private static final int VIEW_STATE_PROGRESS = 2;
    private static final int VIEW_STATE_ERROR = 3;
    private static final int VIEW_STATE_EMPTY = 3;

    private int viewState; // used to save current visible view state

    // main views
    private View mainView;
    private View progressView;
    private View errorView;
    private View emptyView;
    private SwipeRefreshLayout swipeLayout;

    // error view components
    private ImageButton buttonRefresh;
    private TextView textError;
    private TextView textTapToRefresh;
    private String errorMsg;

    // empty view components
    private TextView textEmpty;
    private String emptyMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        // init components
        mainView = findViewById(R.id.view_main);
        progressView = findViewById(R.id.view_progress);
        errorView = findViewById(R.id.view_error);
        emptyView = findViewById(R.id.view_empty);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        buttonRefresh = (ImageButton) errorView.findViewById(R.id.button_refresh);
        textError = (TextView) errorView.findViewById(R.id.text_error);
        textTapToRefresh = (TextView) errorView.findViewById(R.id.text_tapToRefresh);
        textEmpty = (TextView) emptyView.findViewById(R.id.text_empty);


        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textError.setTypeface(typeface);
        textTapToRefresh.setTypeface(typeface);
        textEmpty.setTypeface(typeface);

        // customize swipe layout
        swipeLayout.setColorSchemeResources(
                R.color.primary,
                R.color.dark_gray,
                R.color.primary_dark
        );

        // add action listeners
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ProgressActivity.this.onRefresh();
            }
        });

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefresh();
            }
        });
    }

    /**
     * abstract method to pass layout resource id from children
     */
    protected abstract int getLayoutResource();

    /*
    * method used to show main viewState
    */
    protected void showMain() {
        // hide error viewState if it is visible
        ViewUtil.showView(errorView, false);
        // hide empty view if it is visible
        ViewUtil.showView(emptyView, false);
        // hide all AppMsgs
        AppMsg.cancelAll(this);

        // hide progress viewState if it is visible
        ViewUtil.showView(progressView, false);
        // stop swipe layout refreshing if it is
        if (swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(false);

        // show main viewState
        ViewUtil.showView(mainView, true);
        // update viewState state
        viewState = VIEW_STATE_MAIN;
    }

    /*
     * method used to show progress if it is possible
     */
    protected void showProgress() {
        // check to ensure main view is not visible
        if (viewState != VIEW_STATE_MAIN) {
            // not visible, so hide all views
            ViewUtil.showView(mainView, false);
            ViewUtil.showView(errorView, false);
            ViewUtil.showView(emptyView, false);
            // and show progress view
            ViewUtil.showView(progressView, true);

            // update view state
            viewState = VIEW_STATE_PROGRESS;
        } else {
            // main view is visible >> hide all AppMsgs and swipe layout will show its progress
            AppMsg.cancelAll(this);
        }
    }

    /*
     * overloaded method used to show error with default msg
     */
    protected void showError() {
        errorMsg = getString(R.string.error_loading_data);
        // set error text
        textError.setText(errorMsg);

        // show the suitable error style
        showTheError();
    }

    /*
     * overloaded method used to show error with String msg
     */
    protected void showError(String errorMsg) {
        this.errorMsg = errorMsg;
        // set error text
        textError.setText(errorMsg);

        // show the suitable error style
        showTheError();
    }

    /*
     * overloaded method used to show error with msg resource id
     */
    protected void showError(int errorMsgResource) {
        errorMsg = getString(errorMsgResource);
        // set error text
        textError.setText(errorMsgResource);

        // show the suitable error style
        showTheError();
    }

    /*
     * method used to show the suitable error msg
     */
    private void showTheError() {
        // check if main view is visible
        if (viewState == VIEW_STATE_MAIN) {
            // visible, so stop swipe layout refreshing if it is
            if (swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);

            // and hide all other AppMsgs
            AppMsg.cancelAll(this);
            // and just show error in AppMsg
            AppMsg appMsg = AppMsg.makeText(this, errorMsg, AppMsg.STYLE_CONFIRM);
            appMsg.setParent((ViewGroup) mainView);
            appMsg.show();
        } else {
            // main view is not visible, so hide all views
            ViewUtil.showView(mainView, false);
            ViewUtil.showView(progressView, false);

            // and show error view
            ViewUtil.showView(errorView, true);

            // update view state
            viewState = VIEW_STATE_ERROR;
        }
    }

    /*
     * overloaded method used to show empty with default msg
     */
    protected void showEmpty() {
        emptyMsg = getString(R.string.no_data);
        // set empty text
        textEmpty.setText(emptyMsg);

        // show the suitable empty style
        showTheEmpty();
    }

    /*
     * overloaded method used to show empty with String msg
     */
    protected void showEmpty(String emptyMsg) {
        this.emptyMsg = emptyMsg;
        // set empty text
        textEmpty.setText(emptyMsg);

        // show the suitable empty style
        showTheEmpty();
    }

    /*
     * overloaded method used to show empty with msg resource id
     */
    protected void showEmpty(int emptyMsgResId) {
        emptyMsg = getString(emptyMsgResId);
        // set empty text
        textEmpty.setText(emptyMsgResId);

        // show the suitable empty style
        showTheEmpty();
    }

    /*
     * method used to show the suitable empty msg
     */
    private void showTheEmpty() {
        // main view is not visible, so hide all views
        ViewUtil.showView(mainView, false);
        ViewUtil.showView(progressView, false);
        ViewUtil.showView(errorView, false);

        // and show empty view
        ViewUtil.showView(emptyView, true);

        // update view state
        viewState = VIEW_STATE_EMPTY;
    }

    /**
     * abstract method to override in children to do refresh operation
     */
    protected abstract void onRefresh();
}
