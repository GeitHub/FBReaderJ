package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import java.util.Date;

/**
 * Created by animal@martus.org on 5/2/16.
 */
public class ReadingListTitleItem extends AbstractTitleListRowItem {
    private int bookshareId;
    private String readingListBookName;
    private String readingListBookAuthors;
    private Date compareDate;

    public ReadingListTitleItem(int bookshareIdToUse, String readingListNameToUse, String readingListBookAuthorsToUse, Date compareDate) {
        bookshareId = bookshareIdToUse;
        readingListBookName = readingListNameToUse;
        readingListBookAuthors = readingListBookAuthorsToUse;
        this.compareDate = compareDate;
    }

    @Override
    public int getBookId() {
        return bookshareId;
    }

    @Override
    public String getBookTitle() {
        return readingListBookName;
    }

    @Override
    public String getAuthors() {
        return readingListBookAuthors;
    }

    @Override
    public ZLFile getBookZlFile() {
        return null;
    }

    @Override
    public Book getBook() {
        return null;
    }

    public String getBookFilePath() {
        return null;
    }

    @Override
    public boolean isDownloadedBook() {
        return false;
    }

    @Override
    public Date getCompareDate(){
        return compareDate;
    }
}