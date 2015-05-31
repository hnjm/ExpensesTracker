package com.amilabs.android.expensestracker.tasks;

import com.amilabs.android.expensestracker.database.Data;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.utils.Utils;

//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

//import java.io.DataInputStream;
import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVWriter;

public class ExportDBToXLSTask extends AsyncTask<Integer, Void, Boolean> {

    private static final String TAG = "ExportDBToXLSTask";

    private Context context;
    private static DatabaseHandler mDb;
    private final ProgressDialog dialog;
    private File file;

    public ExportDBToXLSTask(Context context) {
        this.context = context;
        dialog = new ProgressDialog(context);
    }
    
    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Exporting to CSV...");
        this.dialog.show();
    }

    @Override
    protected Boolean doInBackground(final Integer... args) {
        File dbFile = context.getDatabasePath(Data.DB_NAME);
        mDb = DatabaseHandler.getInstance(context);
        Log.d(TAG, dbFile + "");

        File exportDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.amilabs.android.expensestracker", "");
        if (!exportDir.exists())
            exportDir.mkdirs();
        Log.d(TAG, exportDir + "");
        file = new File(exportDir, "Expenses.csv");

        try {
            // export to CSV
        	file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = mDb.getExpensesData(args[0]);
            if (curCSV == null)
                return false;
            csvWrite.writeNext(new String[] {"Date", "Expense", "Details", "Category"});
            while (curCSV.moveToNext()) {
                String date = Utils.getStringDate(curCSV.getLong(curCSV.getColumnIndex(Data.Expenses.DATE)));
                String expense = Utils.getFormatted(curCSV.getDouble(curCSV.getColumnIndex(Data.Expenses.EXPENSE))) + "";
                String details = curCSV.getString(curCSV.getColumnIndex(Data.Expenses.DETAILS));
                String category = curCSV.getString(curCSV.getColumnIndex(Data.Categories.NAME));
                String arrStr[] = { date, expense, details, category };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            
            // export from CSV to XLS
            /*ArrayList<ArrayList<String>> arList = null;
            ArrayList<String> al = null;
            String thisLine;
            FileInputStream fis = new FileInputStream(fileCSV);
            DataInputStream myInput = new DataInputStream(fis);
            arList = new ArrayList<ArrayList<String>>();
            while ((thisLine = myInput.readLine()) != null) {
                al = new ArrayList<String>();
                String strar[] = thisLine.split(",");
                for (int j = 0; j < strar.length; j++)
                    al.add(strar[j]);
                arList.add(al);
            }

            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("Expenses");
            for (int k = 0; k < arList.size(); k++) {
                ArrayList<String> ardata = arList.get(k);
                HSSFRow row = sheet.createRow(k);
                for (int p = 0; p < ardata.size(); p++) {
                    HSSFCell cell = row.createCell(p);
                    String data = ardata.get(p).toString();
                    if (data.startsWith("=")) {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        data = data.replaceAll("\"", "");
                        data = data.replaceAll("=", "");
                        cell.setCellValue(data);
                    } else if(data.startsWith("\"")) {
                        data = data.replaceAll("\"", "");
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(data);
                    } else {
                        data = data.replaceAll("\"", "");
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(data);
                    }
                }
            }
            file = new File(exportDir, "Expenses.xls");
            FileOutputStream fileOut = new FileOutputStream(file);
            hwb.write(fileOut);
            fileOut.close();
            fileCSV.delete();*/
            
            return true;
        } catch(SQLException sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
            return false;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing())
            dialog.dismiss();
        if (success)
            Toast.makeText(context, "Exported to " + file, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show();
    }
}
