package com.example.screenextender.clientmanager.clientgraph;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.screenextender.HostActivity;
import com.example.screenextender.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.himanshusoni.quantityview.QuantityView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GraphFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {
    GridView androidGridView;
    QuantityView rowsQuantityView;
    QuantityView colsQuantityView;
    String[] gridViewString;
    int[] gridViewImageId;
    CustomGridViewActivity adapterViewAndroid;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    private int numRows;
    private int numCols;

    private OnFragmentInteractionListener mListener;

    public GraphFragment() {
        // Required empty public constructor
    }

    public static GraphFragment newInstance(String param1, String param2) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
       ArrayList<HostActivity.DeviceInfo> clientsList = getArguments().getParcelableArrayList("clientlist");

       final String[] phoneNames = new String[clientsList.size()+1];
       final String[] phoneIds = new String[clientsList.size()+1];
       for (int i = 0; i < clientsList.size(); i++) {
           phoneNames[i] = clientsList.get(i).getName();
           phoneIds[i] = clientsList.get(i).getId();
       }
       phoneNames[clientsList.size()] = "Host";
       phoneIds[clientsList.size()] = "Host";


        rowsQuantityView=(QuantityView)getView().findViewById(R.id.quantityView_rows);
        colsQuantityView=(QuantityView)getView().findViewById(R.id.quantityView_cols);
        numCols = colsQuantityView.getQuantity();
        numRows = rowsQuantityView.getQuantity();
        generateGrid(phoneNames, phoneIds);

        rowsQuantityView.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
                numRows = newQuantity;
                generateGrid(phoneNames, phoneIds);
            }

            @Override
            public void onLimitReached() {

            }
        });
        colsQuantityView.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
                numCols = newQuantity;
                generateGrid(phoneNames, phoneIds);
            }

            @Override
            public void onLimitReached() {

            }
        });
    }

    private void generateGrid(final String[] phoneNames, final String[] phoneIds) {
        int numPhones = rowsQuantityView.getQuantity()*colsQuantityView.getQuantity();
        gridViewString = new String[numPhones];
        Arrays.fill(gridViewString, "Select...");
        gridViewImageId = new int[numPhones];
        Arrays.fill(gridViewImageId, R.drawable.ic_horizontal_phone);

        adapterViewAndroid = new CustomGridViewActivity(this.getContext(), gridViewString, gridViewImageId);

        androidGridView=(GridView)getView().findViewById(R.id.grid_view_image_text);
        androidGridView.setNumColumns(colsQuantityView.getQuantity());
        androidGridView.setAdapter(adapterViewAndroid);
        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int i, long id) {
                //Toast.makeText(GridViewImageTextActivity.this, "GridView Item: " + gridViewString[+i], Toast.LENGTH_LONG).show();
                //String[] testPhones = {"Test Phone 1", "Test Phone 2", "Test Phone 3", "Test Phone 4"};
                showPhoneSelectionDialog(phoneNames, phoneIds, i);
            }
        });
    }

    private void showPhoneSelectionDialog(final String[] phoneNames, final String[] phoneIds, final int index) {
        getActivity().runOnUiThread(new Runnable() {
            public void run()
            {

                final HashMap<Integer, TextView> textFields = adapterViewAndroid.getTextFields();
                new AlertDialog.Builder(getContext())
                        .setTitle("Pick phone to go in slot # "+(index + 1))
                        .setItems(phoneNames, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                gridViewString[index] = phoneNames[which];
                                HashMap<Integer, TextView> textFields = adapterViewAndroid.getTextFields();
                                TextView newTextView = textFields.get(index);

                                HashMap<String, Integer> selectedPhones = adapterViewAndroid.getSelectedPhones();
                                if (!(newTextView.getText().equals("Select..."))){
                                    selectedPhones.remove(newTextView.getText());
                                    newTextView.setText("Select...");
                                }
                                newTextView.setText(phoneNames[which]);
                                if (adapterViewAndroid.getSelectedPhones().containsKey(phoneIds[which])) {
                                    //HashMap<String, Integer> selectedPhones = adapterViewAndroid.getSelectedPhones();
                                    TextView selectTextView = textFields.get(selectedPhones.get(phoneNames[which]));
                                    selectTextView.setText("Select...");
                                    //selectedPhones.remove(phoneNames[which]);
                                    selectedPhones.put(phoneIds[which], index);
                                } else {
                                    //HashMap<String, Integer> selectedPhones = adapterViewAndroid.getSelectedPhones();
                                    selectedPhones.put(phoneIds[which], index);
                                }

                            }
                        })

                        .show();
            }
        });


    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public String[] getIds(){
        String[] phoneIdsInOrder = new String[adapterViewAndroid.getSelectedPhones().size()];
        for (String s : adapterViewAndroid.getSelectedPhones().keySet()) {
            phoneIdsInOrder[adapterViewAndroid.getSelectedPhones().get(s)] = s;
        }
        return phoneIdsInOrder;
    }
}
