package com.amustware.insertcontacts.app;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener
{
  private static final String DEFAULT_PREFIX = "TestAkk";
  private static final int COUNT = 1000;
  private static final long m_numberBegin = 5550000000L;
  private static final String TAG = MainActivity.class.getName();

  private EditText m_prefix;
  private Button m_go;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    m_prefix = (EditText)findViewById(R.id.etPrefix);
    m_go = (Button)findViewById(R.id.btnGo);
    m_go.setOnClickListener(this);
  }


  @Override
  public void onClick(View view)
  {
    if (view.getId() != R.id.btnGo)
    {
      return;
    }

    String prefix = m_prefix.getText().toString();
    if (prefix == null || prefix.isEmpty())
    {
      prefix = DEFAULT_PREFIX;
    }

    for (int i = 0; i < COUNT; ++i)
    {
      InsertContact(prefix, i);
    }
  }

  private void InsertContact(String prefix, int index)
  {
    String name = prefix + Integer.toString(index);

    int numberShift = index * 3;

    long mobNumber = m_numberBegin + numberShift;
    long homeNumber = m_numberBegin + numberShift + 1;
    long workNumber = m_numberBegin + numberShift + 2;

    int contactId = 0;

    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());


    ops.add(ContentProviderOperation.newInsert(
        ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        .withValue(ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        .withValue(
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name).build());


    //------------------------------------------------------ Mobile Number
    ops.add(ContentProviderOperation.
        newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        .withValue(ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Long.toString(mobNumber))
        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        .build());

    //------------------------------------------------------ Home Numbers
      ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
          .withValue(ContactsContract.Data.MIMETYPE,
              ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Long.toString(homeNumber))
          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
              ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
          .build());

    //------------------------------------------------------ Work Numbers
      ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
          .withValue(ContactsContract.Data.MIMETYPE,
              ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, Long.toString(workNumber))
          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
              ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
          .build());

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setColor(Color.WHITE);
    paint.setStyle(Paint.Style.FILL);
    canvas.drawPaint(paint);

    paint.setColor(Color.BLACK);
    paint.setTextSize(16);
    canvas.drawText(Integer.toString(index), 10, 25, paint);

    bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

      // Adding insert operation to operations list
      // to insert Photo in the table ContactsContract.Data
    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
        .build());

      try
      {
        stream.flush();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

    // Asking the Contact provider to create a new contact
    try
    {
      ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
      for (ContentProviderResult result : results)
      {
        Log.d(TAG, result.toString());
      }
      Log.d(TAG, "Succeed adding contact: " + Integer.toString(index));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }
}
