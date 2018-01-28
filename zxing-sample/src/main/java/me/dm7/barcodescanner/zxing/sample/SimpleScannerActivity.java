package me.dm7.barcodescanner.zxing.sample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;



public class SimpleScannerActivity extends BaseScannerActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_simple_scanner);
        setupToolbar();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        //bookSearcher = new BookSearcher();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {

        BookInfo book = new BookInfo(rawResult.getText(), rawResult.getBarcodeFormat().toString());
        new BookSearchTask(this).execute(book);
        Toast.makeText(this, "Contents = " + rawResult.getText() +
                ", Format = " + rawResult.getBarcodeFormat().toString() + ", getting book information ..." , Toast.LENGTH_SHORT).show();

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(SimpleScannerActivity.this);
            }
        }, 2000);
    }

    public class BookSearchTask extends AsyncTask<BookInfo, Void, BookInfo> {
        private Exception exception;

        private final WeakReference<Activity> weakActivity;

        public BookSearchTask (Activity context){
            this.weakActivity = new WeakReference<>(context);
        }

        protected BookInfo doInBackground(BookInfo... books) {
            try {
                BookInfo book = books[0];
                book.title  = BookSearcher.Send(book.bookId, book.codeType );

                return book;
            } catch (Exception e) {
                this.exception = e;

                return null;
            } finally {

            }
        }


        protected void onPostExecute(BookInfo book) {

            // Re-acquire a strong reference to the activity, and verify
            // that it still exists and is active.
            Activity activity = weakActivity.get();
            if (activity == null
                    || activity.isFinishing()
                    || activity.isDestroyed()) {
                // activity is no longer valid, don't do anything!
                System.out.println("The activity no longer exsits, quietly quite.");
                return;
            }


            System.out.println(book);
            String displayContent = book.toString();

            Toast.makeText(activity, displayContent, Toast.LENGTH_LONG).show();
        }
    }
}
