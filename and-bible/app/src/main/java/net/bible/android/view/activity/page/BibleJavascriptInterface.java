package net.bible.android.view.activity.page;

import android.util.Log;
import android.webkit.JavascriptInterface;

import net.bible.android.control.PassageChangeMediator;
import net.bible.android.control.page.ChapterVerse;
import net.bible.android.control.page.CurrentPageManager;
import net.bible.android.control.page.window.WindowControl;
import net.bible.android.view.activity.base.Callback;
import net.bible.android.view.activity.page.actionmode.VerseActionModeMediator;

/**
 * Interface allowing javascript to call java methods in app
 *
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class BibleJavascriptInterface {

	private boolean notificationsEnabled = false;

	private boolean addingContentAtTop = false;

	private VerseCalculator verseCalculator;

	private ChapterVerse prevCurrentChapterVerse = new ChapterVerse(0,0);

	private final VerseActionModeMediator verseActionModeMediator;
	
	private final WindowControl windowControl;

	private final CurrentPageManager currentPageManager;

	private final BibleInfiniteScrollPopulator bibleInfiniteScrollPopulator;

	private static final String TAG = "BibleJavascriptIntrfc";

	public BibleJavascriptInterface(VerseActionModeMediator verseActionModeMediator, WindowControl windowControl, VerseCalculator verseCalculator, CurrentPageManager currentPageManager, BibleInfiniteScrollPopulator bibleInfiniteScrollPopulator) {
		this.verseActionModeMediator = verseActionModeMediator;
		this.windowControl = windowControl;
		this.verseCalculator = verseCalculator;
		this.currentPageManager = currentPageManager;
		this.bibleInfiniteScrollPopulator = bibleInfiniteScrollPopulator;
	}

	@JavascriptInterface
	public void onLoad() {
		Log.d(TAG, "onLoad from js");
	}

	@JavascriptInterface
	public void onScroll(int newYPos) {
		// do not try to change verse while the page is changing - can cause all sorts of errors e.g. selected verse may not be valid in new chapter and cause chapter jumps
		if (notificationsEnabled && !addingContentAtTop && !PassageChangeMediator.getInstance().isPageChanging() && !windowControl.isSeparatorMoving()) {
			if (currentPageManager.isBibleShown()) {
				//TODO use chapter and verse to change chapter if necessary
				ChapterVerse currentChapterVerse = verseCalculator.calculateCurrentVerse(newYPos);
				if (currentChapterVerse != prevCurrentChapterVerse) {
					currentPageManager.getCurrentBible().setCurrentChapterVerse(currentChapterVerse);
					prevCurrentChapterVerse = currentChapterVerse;
				}
			}
		}
	}
	
	@JavascriptInterface
	public void clearVersePositionCache() {
		Log.d(TAG, "clear verse positions");
		verseCalculator.init();
	}

	@JavascriptInterface
	public void registerVersePosition(String chapterVerseId, int offset) {
		verseCalculator.registerVersePosition(new ChapterVerse(chapterVerseId), offset);
	}

	@JavascriptInterface
	public void verseLongPress(int verse) {
		Log.d(TAG, "Verse selected event:"+verse);
		verseActionModeMediator.verseLongPress(verse);
	}

	@JavascriptInterface
	public void verseTouch(int verse) {
		Log.d(TAG, "Verse touched event:"+verse);
		verseActionModeMediator.verseTouch(verse);
	}

	@JavascriptInterface
	public void requestMoreTextAtTop(String textId) {
		Log.d(TAG, "Request more text at top:"+textId);
		addingContentAtTop = true;
		bibleInfiniteScrollPopulator.requestMoreTextAtTop(textId, new Callback() {
			@Override
			public void okay() {
				addingContentAtTop = false;
			}
		});
	}

	@JavascriptInterface
	public void requestMoreTextAtEnd(String textId) {
		Log.d(TAG, "Request more text at end:"+textId);
		bibleInfiniteScrollPopulator.requestMoreTextAtEnd(textId);
	}

	@JavascriptInterface
	public void log(String msg) {
		Log.d(TAG, msg);
	}

	public void setNotificationsEnabled(boolean notificationsEnabled) {
		this.notificationsEnabled = notificationsEnabled;
	}
}
