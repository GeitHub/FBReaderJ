package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.fbreader.library.ReadingListBook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by animal@martus.org on 4/6/16.
 */
public class ReadingListFragment extends TitleListFragmentWithContextMenu {

    public static final String ARG_SHOULD_ADD_FAVORITES = "shouldAddFavorites";

    private ReadingList readingList;

    private boolean shouldAddFavorites = false;

    public void setReadingList(ReadingList readingListToUse) {
        readingList = readingListToUse;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(getArguments() != null){
            shouldAddFavorites = getArguments().getBoolean(ARG_SHOULD_ADD_FAVORITES, false);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void fillListAdapter() {
        ArrayList<ReadingListBook> readingListBooks = readingList.getReadingListBooks();
        for (int index = 0; index < readingListBooks.size(); ++index) {
            ReadingListBook readingListBook = readingListBooks.get(index);
            final String readingListBookTitle = readingListBook.getTitle();
            final String readingListBookAuthors = readingListBook.getAllAuthorsAsString();
            final int bookshareId = readingListBook.getBookId();
            bookRowItems.add(new ReadingListTitleItem(bookshareId, readingListBookTitle, readingListBookAuthors));
        }

        if(shouldAddFavorites) {
            ArrayList<Book> favoriteTitelsOnDevice = getFavoritesOnDevice();
            for (Book favoriteBookOnDevice : favoriteTitelsOnDevice) {
                bookRowItems.add(new DownloadedTitleListRowItem(favoriteBookOnDevice));
            }
        }

        sortListItems();
        setListAdapter(new ReadingListBooksAdapter(getActivity(), bookRowItems));
    }

    private ArrayList<Book> getFavoritesOnDevice() {
        final BooksDatabase db = BooksDatabase.Instance();
        final Map<Long,Book> savedBooksByBookId = new HashMap<>();
        ArrayList<Book> favoriteBooksOnDevice = new ArrayList<>();
        for (long id : db.loadFavoritesIds()) {
            Book book = savedBooksByBookId.get(id);
            if (book == null) {
                book = Book.getById(id);
                if (book != null && !book.File.exists()) {
                    book = null;
                }
            }
            if (book != null) {
                favoriteBooksOnDevice.add(book);
            }
        }

        return favoriteBooksOnDevice;
    }

    public class ReadingListBooksAdapter extends ArrayAdapter<AbstractTitleListRowItem> {

        public ReadingListBooksAdapter(Context context, List<AbstractTitleListRowItem> items) {
            super(context, R.layout.reading_list_book_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.reading_list_book_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.readingListBook = (TextView) convertView.findViewById(R.id.bookTitle);
                viewHolder.readingListBookAuthors = (TextView) convertView.findViewById(R.id.bookAuthorsLabel);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            AbstractTitleListRowItem item = getItem(position);
            viewHolder.readingListBook.setText(item.getBookTitle());
            viewHolder.readingListBookAuthors.setText(item.getAuthors());

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView readingListBook;
        public TextView readingListBookAuthors;
    }
}
