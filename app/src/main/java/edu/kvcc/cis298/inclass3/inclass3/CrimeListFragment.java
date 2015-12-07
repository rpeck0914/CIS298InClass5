package edu.kvcc.cis298.inclass3.inclass3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by dbarnes on 11/2/2015.
 */
public class CrimeListFragment extends Fragment {

    //Class level variable to hold the recycler view
    private RecyclerView mCrimeRecyclerView;

    //Variable to hold an instance of the adapter
    private CrimeAdapter mAdapter;

    private boolean mSubtitleVisible;

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //This will make a new instance of the FetchCrimesTask private
        //class. When the execute method gets called on it, the
        //FetchCrimesTask will start the doInBackground method on a
        //seperate thread automatically for us.
        new FetchCrimesTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get the view from the layouts that will be displayed.
        //Use the inflator to inflate the layout to java code.
        View view = inflater.inflate(R.layout.fragment_crime_list,
                                    container,
                                    false);

        //Get a reference to the recycler view in the layout file
        //Remember that we have to call findViewById on the view
        //that we created above. It is not an automatic method
        //like it was for an Activity
        mCrimeRecyclerView = (RecyclerView) view
                .findViewById(R.id.crime_recycler_view);

        //The recycler view requires that it is given a Layout
        //Manager. The recyclerview is a fairly new control, and
        //is not capable of displaying the list items on the screen.
        //A LinearLayoutManager is required to do that work. Therefore
        //We create a new LinearLayoutManager, and pass it the context
        //of which it needs to operate it. The context is passed by using
        //the getActivity method. Which gets the activity that is
        //hosting this fragment.
        mCrimeRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        //Call the updateUI method to do the work of getting the
        //data from the CrimeLab, setting it up with the adapter,
        //and then adding the adapter to the recycler view.
        updateUI();

        //Return the created view.
        return view;
    }

    //When this Fragment is resumed from a paused state such as
    //returning to this fragments hosting activity from some other
    //activity, this method will get called, and we can use it to
    //update the UI of the fragment.
    @Override
    public void onResume() {
        super.onResume();
        //Update the UI
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;

            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateUI() {
        //Get the collection of data from the crimelab
        //singleton. The get method constructor requires that
        //a context is passed in, so we send it the hosting
        //activity of this fragment.
        CrimeLab crimeLab = CrimeLab.get(getActivity());

        //Get the actaul list of crimes from the CrimeLab class
        List<Crime> crimes = crimeLab.getCrimes();

        //If the adapter hasn't been created yet, we want to create it
        //and set the Adapter for the Recycler view.
        if (mAdapter == null) {

            //Create a new crimeAdapter and send it over the list
            //of crimes. Crime adapter needs the list of crimes so
            //that it can work with the recyclerview to display them.
            mAdapter = new CrimeAdapter(crimes);

            //Take the adapter that we just created, and set it as the
            //adapter that the recycler view is going to use.
            mCrimeRecyclerView.setAdapter(mAdapter);

        //Else, the adapter already exists, so we just need to notify
        //that the data set might have changed. This will
        //automatically update any data changes for us.
        } else {
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();

    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Create a class level variable to hold the view for
        //this holder.

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        private Crime mCrime;

        //Constructor that takes in a View. The parent constructor
        //is called, and then the passed in View is assigned
        //to the class level version
        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            //Do assignment to class level vars. User the findviewbyid
            //method to get access to the various controls we want to do
            //work with.
            mTitleTextView = (TextView)
                    itemView.findViewById(R.id.list_item_crime_title_text_view);

            mDateTextView = (TextView)
                    itemView.findViewById(R.id.list_item_crime_date_text_view);

            mSolvedCheckBox = (CheckBox)
                    itemView.findViewById(R.id.list_item_crime_solved_check_box);
        }

        //Method to take in a instance of a crime, and assign it to the
        //class level version. Then use the class level version to take
        //properties from the crime and assign them to the various
        //view controls.
        public void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
        }

        //This method must be implemented because we have this class
        //implementing the onclicklistener interface. This method will
        //do the work toasting the title of the crime that was clicked on.
        @Override
        public void onClick(View view) {
            //Ask CrimeActivity for an intent that will get the CrimeActivity
            //started. The method requires us to pass the Context, which we can
            //get from calling getActivity(), and the id of the crime we want
            //to start the activity with. Once we have the intent, we call
            //startActivity to start it.
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        //Class level variable to hold the 'data' of our app.
        //This will be the list of crimes
        private List<Crime> mCrimes;

        //Constructor that takes in a list of crimes, and
        //then assigns them to the class level var.
        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Get a Layout Inflator
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            //Inflate the view that we would like to use to display a single
            //list item.
            //Right now, it is a built in android layout called simple_list_item_1
            View view = layoutInflater.inflate(R.layout.list_item_crime,
                    parent, false);
            //Return a new CrimeHolder with the view passed in as a parameter.
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            //Get the crime that is at the index declared by the variable position
            //and assign it to a local crime variable
            Crime crime = mCrimes.get(position);

            //Send the crime over to the bindCrime method that we wrote on
            //the crimeholder class. That method does the work of setting
            //the properties of the crime to the layout controls in the
            //custom layout we made.
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            //Just return the size of the crime list.
            return mCrimes.size();
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    //Private class to do the networking that we need done on a seperate thread
    private class FetchCrimesTask extends AsyncTask<Void, Void, Void> {
        //This is the method that will be executed on the seperate thread
        //Once it completes the onPostExecute method will be called automatically
        @Override
        protected Void doInBackground(Void... voids) {

            //Create a new Crime Fetcher class and call the fetchCrimes method
            //on the instance that is created.
            new CrimeFetcher().fetchCrimes();
            //Just return null for now.
            return null;
        }

        //Method that will automatically get called when the code in
        //doInBackgroud gets done executing.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


}




