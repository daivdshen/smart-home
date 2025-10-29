package com.intelliving.app;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.intelliving.app.R;
import com.intelliving.app.utils.DataItem;
import com.intelliving.app.utils.Utils;
import com.comelitgroup.module.api.CGAddrBookElement;
import com.comelitgroup.module.api.CGModule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;


public class HomeFragment extends Fragment {

    private static final int COL_SIZE = 320;

    private FrameLayout fragmentContainer;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;

    /**
     * Create a new instance of the fragment
     */
    public static HomeFragment newInstance(int index) {
        HomeFragment fragment = new HomeFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = null;
        int frag = getArguments().getInt("index", 0);

        switch (frag){
            case 0:
                view = inflater.inflate(R.layout.fragment_list, container, false);
                initList(view);
                break;
            case 1:
                view = inflater.inflate(R.layout.fragment_list, container, false);
                initListDoorEntry(view,
                        CGModule.getInstance(getActivity().getApplicationContext()).getExternalUnitAddressBook());
                break;

            case 2:
                view = inflater.inflate(R.layout.fragment_list, container, false);
                LinkedList<CGAddrBookElement> list = new LinkedList<>();
                list.addAll(CGModule.getInstance(getActivity().getApplicationContext()).getCameraAddressBook());
                list.addAll(CGModule.getInstance(getActivity().getApplicationContext()).getRtspCameras());
                initListCamera(view, list);
                break;

            case 3:
                view = inflater.inflate(R.layout.fragment_list, container, false);
                initListOpendoor(view,
                        CGModule.getInstance(getActivity().getApplicationContext()).getOpendoorAddressBook());
                break;
            case 4:
                view = inflater.inflate(R.layout.fragment_list, container, false);
                initListOpendoor(view,
                        CGModule.getInstance(getActivity().getApplicationContext()).getActuactorAddressBook());
                break;
        }

        return view;
    }

    /**
     * Init the fragment
     */
    private void initList(View view) {

        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        int mNoOfColumns = Utils.calculateNoOfColumns(getActivity().getApplicationContext(), COL_SIZE);
        gridLayoutManager = new GridLayoutManager(getActivity(), mNoOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        ArrayList<DataItem> itemsData = new ArrayList<>();

        addElement(itemsData,
                CGModule.getInstance(getActivity().getApplicationContext()).getExternalUnitAddressBook(),
                true, DataItem.VipType.EXTERNAL_UNIT);

        addElement(itemsData,
                CGModule.getInstance(getActivity().getApplicationContext()).getOpendoorAddressBook(),
                false, DataItem.VipType.OPENDOOR);

        addElement(itemsData,
                CGModule.getInstance(getActivity().getApplicationContext()).getInternalUnitAddressBook(),
                false, DataItem.VipType.INTERNAL_UNIT);

        addElement(itemsData,
                CGModule.getInstance(getActivity().getApplicationContext()).getSwitchboardAddressBook(),
                false, DataItem.VipType.SWITCHBOARD);

        HomeAdapter adapter = new HomeAdapter(getActivity(), itemsData);
        recyclerView.setAdapter(adapter);
    }

    private void addElement(ArrayList<DataItem> itemsData, LinkedList<CGAddrBookElement> list,
            boolean showPhone, DataItem.VipType type) {

        if (list.size() == 0){
            return;
        }
        int size = (list.size() / 2) + 1;

        for (int i = 0; i < size; i++) {

            int rnd = new Random().nextInt(list.size());
            DataItem item = new DataItem(list.get(rnd).getName(),
                    "",
                    list.get(rnd).getId(),
                    showPhone, type);

            if(!itemsData.contains(item)) {
                itemsData.add(item);
            }
        }

    }

    private void initListDoorEntry(View view, LinkedList<CGAddrBookElement> list) {

        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        int mNoOfColumns = Utils.calculateNoOfColumns(getActivity().getApplicationContext(), COL_SIZE);
        gridLayoutManager = new GridLayoutManager(getActivity(), mNoOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        ArrayList<DataItem> itemsData = new ArrayList<DataItem>();
        for (int i = 0; i < list.size(); i++) {

            DataItem item = new DataItem(list.get(i).getName(),
                    "",
                    list.get(i).getId(),
                    true, DataItem.VipType.EXTERNAL_UNIT);

            itemsData.add(item);
        }

        HomeAdapter adapter = new HomeAdapter(getActivity(), itemsData);
        recyclerView.setAdapter(adapter);

    }

    private void initListOpendoor(View view, LinkedList<CGAddrBookElement> list) {

        if(list == null)
            return;

        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        int mNoOfColumns = Utils.calculateNoOfColumns(getActivity().getApplicationContext(), COL_SIZE);
        gridLayoutManager = new GridLayoutManager(getActivity(), mNoOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        ArrayList<DataItem> itemsData = new ArrayList<DataItem>();
        for (int i = 0; i < list.size(); i++) {

            DataItem item = new DataItem(list.get(i).getName(),
                    "",
                    list.get(i).getId(),
                    false, DataItem.VipType.OPENDOOR);

            itemsData.add(item);
        }

        HomeAdapter adapter = new HomeAdapter(getActivity(), itemsData);
        recyclerView.setAdapter(adapter);
    }

    private void initListCamera(View view, LinkedList<CGAddrBookElement> list) {

        if(list == null)
            return;

        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        int mNoOfColumns = Utils.calculateNoOfColumns(getActivity().getApplicationContext(), COL_SIZE);
        gridLayoutManager = new GridLayoutManager(getActivity(), mNoOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        ArrayList<DataItem> itemsData = new ArrayList<DataItem>();
        for (int i = 0; i < list.size(); i++) {

            DataItem item = new DataItem(list.get(i).getName(),
                    "",
                    list.get(i).getId(),
                    false, DataItem.VipType.OPENDOOR);

            itemsData.add(item);
        }

        HomeAdapter adapter = new HomeAdapter(getActivity(), itemsData);
        recyclerView.setAdapter(adapter);
    }


    /**
     * Refresh
     */
    public void refresh() {
        if (getArguments().getInt("index", 0) > 0 && recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * Refresh
     */
    public void refreshDoorEntry(LinkedList<CGAddrBookElement> list) {
        initListDoorEntry(getView(), list);
        refresh();
    }

    /**
     * Called when a fragment will be displayed
     */
    public void willBeDisplayed() {
        // Do what you want here, for example animate the content
        if (fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            fragmentContainer.startAnimation(fadeIn);
        }
    }

    /**
     * Called when a fragment will be hidden
     */
    public void willBeHidden() {
        if (fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            fragmentContainer.startAnimation(fadeOut);
        }
    }

}
