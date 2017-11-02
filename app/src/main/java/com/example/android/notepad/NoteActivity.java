package com.example.android.notepad;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {

    private boolean mIsViewingOrUpdating;
    private long mNoteCreationTime;
    private String mFileName;
    private Note mLoadedNote = null;

    private EditText mEtTitle;
    private EditText mEtContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mEtTitle = (EditText) findViewById(R.id.noteTitle);
        mEtContent = (EditText) findViewById(R.id.noteContent);

        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        if(mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
            if (mLoadedNote != null) {
                mEtTitle.setText(mLoadedNote.getTitle());
                mEtContent.setText(mLoadedNote.getContent());
                mNoteCreationTime = mLoadedNote.getDateTime();
                mIsViewingOrUpdating = true;
            }
        } else {
            mNoteCreationTime = System.currentTimeMillis();
            mIsViewingOrUpdating = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mIsViewingOrUpdating) {
            getMenuInflater().inflate(R.menu.menu_note_view, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_note_add, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save_note:
            case R.id.action_update:
                validateAndSaveNote();
                break;

            case R.id.action_delete:
                actionDelete();
                break;

            case R.id.action_cancel:
                actionCancel();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        actionCancel();
    }

    private void actionDelete() {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this)
                .setTitle("Delete note")
                .setMessage("Really delete the note?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mLoadedNote != null && Utilities.deleteFile(getApplicationContext(), mFileName)) {
                            Toast.makeText(NoteActivity.this, mLoadedNote.getTitle() + " is deleted"
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NoteActivity.this, "Can not delete the note '" + mLoadedNote.getTitle() + "'", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton("NO", null);

        dialogDelete.show();
    }

    private void actionCancel() {

        if(!checkNoteAltred()) {
            finish();
        } else {
            AlertDialog.Builder dialogCancel = new AlertDialog.Builder(this)
                    .setTitle("Discard changes...")
                    .setMessage("Are you sure you do not want to save changes to this note?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("NO", null);
            dialogCancel.show();
        }
    }

    private boolean checkNoteAltred() {
        if(mIsViewingOrUpdating) {
            return mLoadedNote != null && (!mEtTitle.getText().toString().equalsIgnoreCase(mLoadedNote.getTitle())
                    || !mEtContent.getText().toString().equalsIgnoreCase(mLoadedNote.getContent()));
        } else {
            return !mEtTitle.getText().toString().isEmpty() || !mEtContent.getText().toString().isEmpty();
        }
    }

    private void validateAndSaveNote() {

        String title = mEtTitle.getText().toString();
        String content = mEtContent.getText().toString();

        if(title.isEmpty()) { //title
            Toast.makeText(NoteActivity.this, "Please enter a title!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(content.isEmpty()) {
            Toast.makeText(NoteActivity.this, "Please enter a content for your note!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(mLoadedNote != null) {
            mNoteCreationTime = mLoadedNote.getDateTime();
        } else {
            mNoteCreationTime = System.currentTimeMillis();
        }

        if(Utilities.saveNote(this, new Note(mNoteCreationTime, title, content))) {
            Toast.makeText(this, "Note has been saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Can not save the note. make sure you have enough space " +
                    "on your device", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
