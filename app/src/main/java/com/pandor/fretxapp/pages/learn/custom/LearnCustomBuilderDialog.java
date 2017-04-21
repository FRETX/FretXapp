package com.pandor.fretxapp.pages.learn.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pandor.fretxapp.R;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

/**
 * Created by pandor on 3/7/17.
 */

public class LearnCustomBuilderDialog extends DialogFragment
{
    private static final String SEQUENCES_EXTRA_IDENTIFIER = "sequences";
    private static final String SEQUENCE_INDEX_EXTRA_IDENTIFIER = "sequence_index";
    private static final String DEFAULT_SEQUENCE_NAME = "Unsaved Exercise";

    private Dialog dialog;
    private Spinner spinner;
    private SpinnerSequenceArrayAdapter spinnerAdapter;
    private ListViewSequenceArrayAdapter listViewAdapter;

    private ArrayList<Sequence> sequences;
    private int currentSequenceIndex;

    interface LearnCustomChordExerciseListener {
        void onUpdate(ArrayList<Sequence> sequences, int currentSequenceIndex);
    }

    public static LearnCustomBuilderDialog newInstance(LearnCustomChordExerciseListener listener,
                                                       ArrayList<Sequence> sequences,
                                                       int currentSequencePosition) {
        LearnCustomBuilderDialog dialog = new LearnCustomBuilderDialog();
        dialog.setTargetFragment((Fragment) listener, 1234);
        Bundle args = new Bundle();
        args.putSerializable(SEQUENCES_EXTRA_IDENTIFIER, sequences);
        args.putInt(SEQUENCE_INDEX_EXTRA_IDENTIFIER, currentSequencePosition);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.pages_learn_custom_builder_dialog);

        //retrieve sequences from arguments
        sequences = (ArrayList<Sequence>) getArguments().getSerializable(SEQUENCES_EXTRA_IDENTIFIER);
        currentSequenceIndex = getArguments().getInt(SEQUENCE_INDEX_EXTRA_IDENTIFIER);
        getArguments().remove(SEQUENCES_EXTRA_IDENTIFIER);
        getArguments().remove(SEQUENCE_INDEX_EXTRA_IDENTIFIER);
        Sequence first = sequences.get(0);
        if (first.getName() == null && first.getChords().size() == 0){
            sequences.remove(0);
            --currentSequenceIndex;
        }

        //setup spinner
        spinner = (Spinner) dialog.findViewById(R.id.sequence_selection);
        spinnerAdapter = new SpinnerSequenceArrayAdapter(getActivity(), sequences);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(currentSequenceIndex);

        //setup listView
        ListView listview = (ListView) dialog.findViewById(R.id.chords_listview);
        listViewAdapter = new ListViewSequenceArrayAdapter(getActivity(), new ArrayList<Chord>());
        listview.setAdapter(listViewAdapter);

        //setup listeners
        setOnClickListeners();

        return dialog;
    }

    private void setOnClickListeners() {
        //handle sequence selection though spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Sequence selected = (Sequence)spinner.getSelectedItem();
                listViewAdapter.clear();
                listViewAdapter.addAll(selected.getChords());
                listViewAdapter.notifyDataSetChanged();
                currentSequenceIndex = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                listViewAdapter.clear();
                listViewAdapter.notifyDataSetChanged();
            }
        });

        //delete the selected sequence
        ImageButton ib = (ImageButton) dialog.findViewById(R.id.delete_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItem() != null) {
//                    Toast.makeText(getActivity(), "Delete", Toast.LENGTH_SHORT).show();
                    deleteConfirmationAlertDialogBuilder().show();
                }
            }
        });

        //add a new empty sequence
        ib = (ImageButton) dialog.findViewById(R.id.new_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewExercise();
            }
        });

        //save modifications made on the selected sequence
        Button b = (Button) dialog.findViewById(R.id.save_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItem() != null) {
                    Toast.makeText(getActivity(), "Exercise Saved", Toast.LENGTH_SHORT).show();
                    if (sequences.get(currentSequenceIndex).getName() == null) {
                        NameSelectionAlertDialogBuilder().show();
                    } else {
                        ArrayList<Sequence> save = LearnCustomBuilderJson.load(getContext());
                        Sequence sequence = sequences.get(currentSequenceIndex);
                        String name = sequence.getName();
                        for (int i = 0; i < save.size(); ++i) {
                            if (save.get(i).getName().equals(name)) {
                                save.get(i).setChords(sequence.getChords());
                                LearnCustomBuilderJson.save(getContext(), save);
                                break;
                            }
                        }
                    }
                }
            }
        });

        //save modifications made on the selected sequence under a new name
        b = (Button) dialog.findViewById(R.id.saveas_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NameSelectionAlertDialogBuilder().show();
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialogInterface){
        Fragment parentFragment = getTargetFragment();
        ((LearnCustomChordExerciseListener) parentFragment).onUpdate(sequences, currentSequenceIndex);
    }

    @Nullable
    private ArrayList<Sequence> isSequenceSavable(String name) {
        ArrayList<Sequence> save = LearnCustomBuilderJson.load(getContext());
        for (int i = 0; i < save.size(); ++i) {
            if (save.get(i).getName().equals(name)) {
                return null;
            }
        }
        return save;
    }

    private AlertDialog deleteConfirmationAlertDialogBuilder() {
        final Sequence sequence = sequences.get(currentSequenceIndex);
        final String name = sequence.getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to delete "
                + (name == null ? DEFAULT_SEQUENCE_NAME : name))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (name != null) {
                            ArrayList<Sequence> save =
                                    LearnCustomBuilderJson.load(getContext());
                            for (int i = 0; i < save.size(); ++i) {
                                if (save.get(i).getName().equals(sequence.getName())) {
                                    save.remove(i);
                                    LearnCustomBuilderJson.save(getContext(), save);
                                    break;
                                }
                            }
                        }
                        int position = spinnerAdapter.getPosition(sequence);
                        spinnerAdapter.remove(sequence);
                        spinnerAdapter.notifyDataSetChanged();
                        if(position>0) spinner.setSelection(position-1);

                        if(spinnerAdapter.getCount() == 0){
                            addNewExercise();
                            spinner.setSelection(0);
                        }

                        Sequence selected = (Sequence)spinner.getSelectedItem();
                        listViewAdapter.clear();
                        if (selected != null) {
                            listViewAdapter.addAll(selected.getChords());
                        }
                        listViewAdapter.notifyDataSetChanged();
                        currentSequenceIndex = spinner.getSelectedItemPosition();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    private Dialog NameSelectionAlertDialogBuilder() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.pages_learn_custom_builder_dialog_name);

        final EditText nameEditText = (EditText) dialog.findViewById(R.id.name);
        final Button save = (Button) dialog.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Sequence> save;
                String name = nameEditText.getText().toString();
                if (name.equals("")) {
                    nameEditText.setError("Specify a name");
                } else if ((save = isSequenceSavable(name)) == null) {
                    nameEditText.setError("Name must be unique");
                } else {
                    Sequence sequence = sequences.get(currentSequenceIndex);
                    Sequence newSequence = new Sequence(name, new ArrayList<>(sequence.getChords()));
                    sequences.add(newSequence);
                    save.add(newSequence);
                    if (sequence.getName() == null)
                        sequences.remove(sequence);
                    currentSequenceIndex = sequences.size() - 1;
                    LearnCustomBuilderJson.save(getContext(), save);
                    spinner.setSelection(currentSequenceIndex);
                    spinnerAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }

    private class SpinnerSequenceArrayAdapter extends ArrayAdapter<Sequence> {
        SpinnerSequenceArrayAdapter(Context context, ArrayList<Sequence> values) {
            super(context, android.R.layout.simple_spinner_item, values);
        }

        @Override
        @NonNull
        public TextView getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            String name = sequences.get(position).getName();
            v.setText(name == null ? DEFAULT_SEQUENCE_NAME : name);
            return v;
        }

        @Override
        @NonNull
        public TextView getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            String name = sequences.get(position).getName();
            v.setText(name == null ? DEFAULT_SEQUENCE_NAME : name);
            return v;
        }
    }

    private void addNewExercise(){
//        Toast.makeText(getActivity(), "Add", Toast.LENGTH_SHORT).show();
        Sequence sequence = new Sequence(null, new ArrayList<Chord>());
        spinnerAdapter.add(sequence);
        listViewAdapter.clear();
        listViewAdapter.addAll(sequence.getChords());
        listViewAdapter.notifyDataSetChanged();
        currentSequenceIndex = sequences.size() - 1;
        spinner.setSelection(currentSequenceIndex);
    }

    //Todo: recycle views
    private class ListViewSequenceArrayAdapter extends ArrayAdapter<Chord> {
        private final Context context;

        ListViewSequenceArrayAdapter(Context context, ArrayList<Chord> chords) {
            super(context, R.layout.pages_learn_custom_builder_dialog_item, chords);
            this.context = context;
        }

        @Override
        @NonNull
        public View getView(final int position, final View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //// TODO: 19/04/17 use recycled view
            View rowView = inflater.inflate(R.layout.pages_learn_custom_builder_dialog_item, parent, false);
            TextView textViewName = (TextView) rowView.findViewById(R.id.chordNameTextview);
            textViewName.setText(sequences.get(currentSequenceIndex).getChords().get(position).toString());
            ImageView image = (ImageView) rowView.findViewById(R.id.deleteImageView);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listViewAdapter.remove(sequences.get(currentSequenceIndex).getChords().get(position));
                    sequences.get(currentSequenceIndex).getChords().remove(position);
                }
            });
            return rowView;
        }
    }
}
