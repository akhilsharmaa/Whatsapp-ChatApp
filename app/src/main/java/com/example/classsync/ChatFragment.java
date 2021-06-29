package com.example.classsync;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.ContextCompat.startActivity;


public class ChatFragment extends Fragment{


        private FirebaseFirestore firebaseFirestoreDB;
        LinearLayoutManager linearLayoutManager;
        private FirebaseAuth firebaseAuth;
        ImageView mImageView;
        //    FirestoreRecyclerAdapter<FirebaseModel,NoteViewHolder> chatAdapter;

        ArrayList<FirebaseModel> dataList ;
        RecyclerView mRecyclerView;
        myRecyclerViewAdapter myRecyclerViewAdapter;

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @NonNull Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.chat_fragment, container, false);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestoreDB = FirebaseFirestore.getInstance();
            mRecyclerView = view.findViewById(R.id.chat_recyclerView);

            mRecyclerView = view.findViewById(R.id.chat_recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            dataList = new ArrayList<>();
            myRecyclerViewAdapter  =new myRecyclerViewAdapter(dataList);
            mRecyclerView.setAdapter(myRecyclerViewAdapter);

            firebaseFirestoreDB.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                    for (DocumentSnapshot d: snapshotList){
                        FirebaseModel obj = d.toObject(FirebaseModel.class);
                        dataList.add(obj);
                    }

                    /* TODO: update adapter */
                    myRecyclerViewAdapter.notifyDataSetChanged();

                }
            });


            return view;
        }

}



    /** Adapter  by Md JAMAL*/
    class myRecyclerViewAdapter extends RecyclerView.Adapter<myRecyclerViewAdapter.MyViewHolder> {

    ArrayList<FirebaseModel> dataList;

        public myRecyclerViewAdapter(ArrayList<FirebaseModel> dataList) {
            this.dataList = dataList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_view_layout,parent,false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull myRecyclerViewAdapter.MyViewHolder holder, int position) {

            String nameOfUser_fetch = dataList.get(position).getName();
            String uidOfUser_fetch = dataList.get(position).getUid();
            String imageOfUser_fetch = dataList.get(position).getImage();
            String statusOfUser_fetch = dataList.get(position).getStatus();

            holder.particularUsername.setText(nameOfUser_fetch);
//            holder.particularStatusOfUser.setText(dataList.get(position).getStatus());

//            holder.mImageOfUser.setImageURI(dataList.get(position).getImage());
            Picasso.get().load(dataList.get (position).getImage()).into(holder.mImageOfUser);

            if (dataList.get(position).getStatus().equals("Online")){
                holder.particularStatusOfUser.setText(dataList.get(position).getStatus());
                    holder.particularStatusOfUser.setTextColor(Color.GREEN); }
            else {
                holder.particularStatusOfUser.setText(dataList.get(position).getStatus());
                }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /** Sending data of clicked user to the  SpecificChatActivity */
                        FirebaseModel firebaseModel = new FirebaseModel();

                        Intent intent = new Intent(v.getContext(),SpecificChatActivity.class);
                        intent.putExtra("ReceiverName",nameOfUser_fetch);
                        intent.putExtra("ReceiverUid",uidOfUser_fetch);
                        intent.putExtra("ReceiverImageUri",imageOfUser_fetch);
                        intent.putExtra("ReceiverStatus",statusOfUser_fetch);
                        v.getContext().startActivity(intent);

//                        Toast.makeText(v.getContext(), "Item Clicked : " + nameOfUser_fetch + " , "
//                                + statusOfUser_fetch + " uid: " + uidOfUser_fetch, Toast.LENGTH_SHORT).show();


                    }
                });
        }


        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder{


        private TextView particularUsername;
        private TextView particularStatusOfUser;
        private ImageView mImageOfUser;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            particularUsername = itemView.findViewById(R.id.nameOfUser);
            particularStatusOfUser = itemView.findViewById(R.id.statusOfUser);
            mImageOfUser = itemView.findViewById(R.id.imageViewOfUser);


        }
        }
    }



/** Code by Tech project youtube channel **/

//        Query query = firebaseFirestore.collection("Users");
//        FirestoreRecyclerOptions<FirebaseModel> allUserName = new FirestoreRecyclerOptions
//                .Builder<FirebaseModel>().setQuery(query,FirebaseModel.class).build();
//
//        chatAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUserName) {
//            @Override
//            protected void onBindViewHolder(@NonNull  NoteViewHolder noteViewHolder, int position, @NonNull FirebaseModel firebaseModel) {
//
//                noteViewHolder.particularUsername.setText(firebaseModel.getName());
//                String uri = firebaseModel.getImage();
//
//                Picasso.get().load(uri).into(mImageView);
//                if (firebaseModel.getStatus().equals("Online")){
//                    noteViewHolder.particularStatusOfUser.setText(firebaseModel.getStatus());
//                    noteViewHolder.particularStatusOfUser.setTextColor(Color.GREEN);
//                }else {
//                    noteViewHolder.particularStatusOfUser.setText(firebaseModel.getStatus());
//                }
//
//                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(getActivity(), "Item Clicked", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//
//            @NonNull
//            @Override
//            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.chat_view_layout,parent,false);
//                return new NoteViewHolder(view);
//            }
//        };
//
//        mRecyclerView.setHasFixedSize(true);
//        linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
//        mRecyclerView.setLayoutManager(linearLayoutManager);
//        mRecyclerView.setAdapter(chatAdapter);
//        return view;
//
//    }
//
//    public class  NoteViewHolder extends RecyclerView.ViewHolder{
//
//        private TextView particularUsername;
//        private TextView particularStatusOfUser;
//        private ImageView mImageOfUser;
//
//        public NoteViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            particularUsername = itemView.findViewById(R.id.nameOfUser);
//            particularStatusOfUser = itemView.findViewById(R.id.statusOfUser);
//            mImageOfUser = itemView.findViewById(R.id.imageViewOfUser);
//
//        }
//    }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            chatAdapter.startListening();
//        }
//
//        @Override
//        public void onStop() {
//            super.onStop();
//            if (chatAdapter!= null){
//                chatAdapter.stopListening();
//            }
//        }
//    }
