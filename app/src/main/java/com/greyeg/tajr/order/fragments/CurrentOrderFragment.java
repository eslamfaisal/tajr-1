package com.greyeg.tajr.order.fragments;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.greyeg.tajr.R;
import com.greyeg.tajr.activities.LoginActivity;
import com.greyeg.tajr.activities.OrderActivity;
import com.greyeg.tajr.helper.SharedHelper;
import com.greyeg.tajr.helper.ViewAnimation;
import com.greyeg.tajr.models.DeleteAddProductResponse;
import com.greyeg.tajr.models.RemainingOrdersResponse;
import com.greyeg.tajr.order.CurrentOrderData;
import com.greyeg.tajr.order.NewOrderActivity;
import com.greyeg.tajr.order.adapters.MultiOrderProductsAdapter;
import com.greyeg.tajr.order.adapters.SignleOrderProductsAdapter;
import com.greyeg.tajr.order.enums.OrderProductsType;
import com.greyeg.tajr.order.enums.ResponseCodeEnums;
import com.greyeg.tajr.order.models.City;
import com.greyeg.tajr.order.models.CurrentOrderResponse;
import com.greyeg.tajr.order.models.Order;
import com.greyeg.tajr.order.models.SingleOrderProductsResponse;
import com.greyeg.tajr.server.Api;
import com.greyeg.tajr.server.BaseClient;
import com.greyeg.tajr.view.dialogs.Dialogs;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.greyeg.tajr.activities.LoginActivity.IS_LOGIN;
import static com.greyeg.tajr.view.dialogs.Dialogs.showProgressDialog;

public class CurrentOrderFragment extends Fragment {

    private final String TAG = "CurrentOrderFragment";
    @BindView(R.id.client_name)
    EditText client_name;
    @BindView(R.id.client_address)
    EditText client_address;
    @BindView(R.id.client_area)
    EditText client_area;
    @BindView(R.id.item_no)
    EditText item_no;
    @BindView(R.id.client_order_phone1)
    EditText client_order_phone1;
    @BindView(R.id.status)
    EditText status;
    @BindView(R.id.shipping_status)
    EditText shipping_status;
    @BindView(R.id.shipping_cost)
    EditText shipping_cost;
    @BindView(R.id.sender_name)
    EditText sender_name;
    @BindView(R.id.item_cost)
    EditText item_cost;
    @BindView(R.id.ntes)
    EditText notes;
    @BindView(R.id.discount)
    EditText discount;
    @BindView(R.id.order_total_cost)
    EditText order_total_cost;
    @BindView(R.id.client_feedback)
    EditText client_feedback;
    @BindView(R.id.order_id)
    EditText order_id;
    @BindView(R.id.client_city)
    Spinner client_city;
    @BindView(R.id.add_product)
    ImageView add_product;
    @BindView(R.id.order_type)
    EditText order_type;
    @BindView(R.id.single_order_product_spinner)
    Spinner single_order_product_spinner;
    @BindView(R.id.ProgressBar)
    ProgressBar mProgressBar4;
    @BindView(R.id.present)
    TextView present;

    @BindView(R.id.fabShowUpdateOrderButtons)
    FloatingActionButton fabShiwUpdateOrderButtons;
    @BindView(R.id.back_drop)
    View back_drop;
    // multi orders
    @BindView(R.id.products_recycler_view)
    RecyclerView multiOrderroductsRecyclerView;
    @BindView(R.id.client_cancel)
    View client_cancel;

    @BindView(R.id.confirm_data)
    View order_data_confirmed;
    // main view of the CurrentOrderFragment
    private View mainView;
    private LinearLayoutManager multiOrderProductsLinearLayoutManager;
    private MultiOrderProductsAdapter multiOrderProductsAdapter;
    private boolean firstOrder;
    private int firstRemaining;
    private Dialog errorGetCurrentOrderDialog;
    private boolean rotate = false;

    public CurrentOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_current_order, container, false);
        ButterKnife.bind(this, mainView);
        initLabels();
        getCurrentOrder();
        setListeners();
        return mainView;
    }

    private void setListeners() {
        add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProductToMultiOrdersTv();
            }
        });
        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back_drop.setVisibility(View.GONE);
                toggleFabMode(fabShiwUpdateOrderButtons);
            }
        });
        fabShiwUpdateOrderButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fabShiwUpdateOrderButtons);
            }
        });

        
    }

    void addProductToMultiOrdersTv() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.layout_add_rpoduct_dialog);

        Spinner productSpinner = dialog.findViewById(R.id.product_spinner);
        productSpinner.setTag(OrderProductsType.MuhltiOrder.getType());
        EditText productNo = dialog.findViewById(R.id.product_no);
        TextView addProductBtn = dialog.findViewById(R.id.add_product);
        productNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        fillSpinnerWithProduts(productSpinner);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                addProductToMultiOrder(Integer.valueOf(productNo.getText().toString()), productSpinner.getSelectedItemPosition());
            }
        });
        dialog.show();

    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(client_cancel);
            ViewAnimation.showIn(order_data_confirmed);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(client_cancel);
            ViewAnimation.showOut(order_data_confirmed);
            back_drop.setVisibility(View.GONE);
        }
    }

    private void addProductToMultiOrder(int number, int index) {
        ProgressDialog progressDialog = showProgressDialog(getActivity(), getString(R.string.add_product));
        BaseClient.getBaseClient().create(Api.class).addProduct(
                SharedHelper.getKey(getActivity(), LoginActivity.TOKEN),
                CurrentOrderData.getInstance().getCurrentOrderResponse().getOrder().getId(),
                CurrentOrderData.getInstance().getSingleOrderProductsResponse().getProducts().get(index).getProductId(),
                CurrentOrderData.getInstance().getCurrentOrderResponse().getUserId(),
                String.valueOf(number)
        ).enqueue(new Callback<DeleteAddProductResponse>() {
            @Override
            public void onResponse(Call<DeleteAddProductResponse> call, Response<DeleteAddProductResponse> response) {
                progressDialog.dismiss();
                if (response.body() != null) {

                    if (response.body().getCode().equals(ResponseCodeEnums.code_1200.getCode())) {
                        Toast.makeText(getActivity(), getString(R.string.added_success), Toast.LENGTH_SHORT).show();
                        getCurrentOrder();
                    }

                } else {
                    errorGetCurrentOrderDialog = Dialogs.showCustomDialog(getActivity(),
                            response.toString(), getString(R.string.order),
                            getString(R.string.retry), getString(R.string.finish_work), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    errorGetCurrentOrderDialog.dismiss();
                                    getCurrentOrder();
                                }
                            }, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    NewOrderActivity.finishWork();
                                }
                            });
                }

            }

            @Override
            public void onFailure(Call<DeleteAddProductResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.d("DeleteAddProduct", "onFailure: " + t.getMessage());
                errorGetCurrentOrderDialog = Dialogs.showCustomDialog(getActivity(),
                        t.getMessage().toString(), getString(R.string.order),
                        getString(R.string.retry), getString(R.string.finish_work), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                errorGetCurrentOrderDialog.dismiss();
                                getCurrentOrder();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NewOrderActivity.finishWork();
                            }
                        });
            }
        });

    }

    private void getCurrentOrder() {

        ProgressDialog progressDialog = showProgressDialog(getActivity(), getString(R.string.fetching_th_order));
        BaseClient.getBaseClient().create(Api.class)
                .getNewCurrentOrderResponce(SharedHelper.getKey(getActivity(), LoginActivity.TOKEN))
                .enqueue(new Callback<CurrentOrderResponse>() {
                    @Override
                    public void onResponse(Call<CurrentOrderResponse> call, Response<CurrentOrderResponse> response) {
                        progressDialog.dismiss();
                        if (response.body() != null) {
                            if (response.body().getCode().equals(ResponseCodeEnums.code_1200.getCode())) {
                                CurrentOrderData.getInstance().setCurrentOrderResponse(response.body());
                                fillFieldsWithOrderData(response.body());
                                updateProgress();
                            } else if (response.body().getCode().equals(ResponseCodeEnums.code_1300.getCode())) {
                                // no new orders all handled
                                Dialogs.showCustomDialog(getActivity(), getString(R.string.no_more_orders), getString(R.string.order),
                                        "Back", null, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                NewOrderActivity.finishWork();
                                            }
                                        }, null);
                            } else if (
                                    response.body().getCode().equals(ResponseCodeEnums.code_1407.getCode()) ||
                                            response.body().getCode().equals(ResponseCodeEnums.code_1408.getCode()) ||
                                            response.body().getCode().equals(ResponseCodeEnums.code_1490.getCode()) ||
                                            response.body().getCode().equals(ResponseCodeEnums.code_1511.getCode()) ||
                                            response.body().getCode().equals(ResponseCodeEnums.code_1440.getCode())) {

                                SharedHelper.putKey(getActivity(), IS_LOGIN, "no");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                NewOrderActivity.finishWork();
                            }

                        } else {
                            showErrorGetCurrentOrderDialog(response.toString());
                            Log.d(TAG, "onResponse: null = " + response.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<CurrentOrderResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        showErrorGetCurrentOrderDialog(t.getMessage());

                    }
                });
    }

    private void showErrorGetCurrentOrderDialog(String msg) {
        errorGetCurrentOrderDialog = Dialogs.showCustomDialog(getActivity(),
                msg, getString(R.string.order),
                getString(R.string.retry), getString(R.string.finish_work), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        errorGetCurrentOrderDialog.dismiss();
                        getCurrentOrder();

                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewOrderActivity.finishWork();
                    }
                });
        Log.d(TAG, "onFailure: " + msg);
    }

    private void fillFieldsWithOrderData(CurrentOrderResponse orderResponse) {

        Order order = orderResponse.getOrder();
        status.setText(order.getOrderStatus());
        client_name.setText(order.getClientName());
        client_address.setText(order.getClientAddress());
        client_area.setText(order.getClientArea());
        shipping_status.setText(order.getOrderShippingStatus());
        client_order_phone1.setText(order.getPhone1());
        order_id.setText(order.getId());
        item_cost.setText(order.getItemCost());
        item_no.setText(order.getItemsNo());
        order_total_cost.setText(order.getTotalOrderCost());
        sender_name.setText(order.getSenderName());
        order_type.setText(order.getOrderType());
        client_feedback.setText(order.getClientFeedback());
        notes.setText(order.getNotes());
        discount.setText(order.getDiscount());
        shipping_cost.setText(order.getShippingCost());

        initCities(orderResponse);
        getSingleOrderProducts();
        if (order.getOrderType().equals(OrderProductsType.SingleOrder.getType())) {
            single_order_product_spinner.setVisibility(View.VISIBLE);
        } else {
            getMultiOrdersProducts();
            single_order_product_spinner.setVisibility(View.GONE);
        }
    }

    private void getMultiOrdersProducts() {
        multiOrderProductsAdapter = new MultiOrderProductsAdapter(getActivity(),
                CurrentOrderData.getInstance().getCurrentOrderResponse().getOrder().getMultiOrders(),
                new MultiOrderProductsAdapter.GetOrderInterface() {
                    @Override
                    public void getOrder() {
                        getCurrentOrder();
                    }
                });
        multiOrderProductsLinearLayoutManager = new LinearLayoutManager(getActivity());
        multiOrderroductsRecyclerView.setLayoutManager(multiOrderProductsLinearLayoutManager);
        multiOrderroductsRecyclerView.setAdapter(multiOrderProductsAdapter);


    }

    private void getSingleOrderProducts() {
        BaseClient.getBaseClient().create(Api.class)
                .getSingleOrderProducts(SharedHelper.getKey(getActivity(), LoginActivity.TOKEN),
                        CurrentOrderData.getInstance().getCurrentOrderResponse().getUserId()
                ).enqueue(new Callback<SingleOrderProductsResponse>() {
            @Override
            public void onResponse(Call<SingleOrderProductsResponse> call, Response<SingleOrderProductsResponse> response) {
                if (response.body() != null) {
                    CurrentOrderData.getInstance().setSingleOrderProductsResponse(response.body());
                    single_order_product_spinner.setTag(OrderProductsType.SingleOrder.getType());
                    fillSpinnerWithProduts(single_order_product_spinner);
                } else {
                    //TODO make dialog
                }
            }

            @Override
            public void onFailure(Call<SingleOrderProductsResponse> call, Throwable t) {

            }
        });
    }

    private void fillSpinnerWithProduts(Spinner spinner) {
        if (CurrentOrderData.getInstance().getSingleOrderProductsResponse() != null) {
            ArrayAdapter adapter = new SignleOrderProductsAdapter(getActivity(),
                    CurrentOrderData.getInstance().getSingleOrderProductsResponse());
            spinner.setSelection(CurrentOrderData.getInstance().getSingleOrderProductsResponse().getProducts().indexOf(
                    CurrentOrderData.getInstance().getCurrentOrderResponse().getOrder().getProductId()
            ));
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (spinner.getTag().equals(OrderProductsType.MuhltiOrder.getType())) {
                        Log.d(TAG, "onItemSelected: from multi " + spinner.getSelectedItemPosition());
                    } else if (spinner.getTag().equals(OrderProductsType.SingleOrder.getType())) {

                        Log.d(TAG, "onItemSelected: from single " + spinner.getSelectedItemPosition());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void initCities(CurrentOrderResponse order) {

        ArrayList<String> citiesNames = new ArrayList<>();
        for (City city : order.getCities()) {
            citiesNames.add(city.getCityName());
        }
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.layout_cities_spinner_item, citiesNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        client_city.setAdapter(adapter);
        int cityIndex = citiesNames.indexOf(order.getOrder().getClientCity());
        if (cityIndex < 0) {
            cityIndex = 0;
        }
        client_city.setSelection(cityIndex);
        client_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                Log.d(TAG, "onItemSelected: " + client_city.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // init hide and show labels
    private void initLabels() {
        LinearLayout linearLayout = mainView.findViewById(R.id.order_fields);

        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            LinearLayout chiledLinearLayout = linearLayout.findViewById(linearLayout.getChildAt(i).getId());
            if (chiledLinearLayout != null)
                chiledLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        runAnimation(chiledLinearLayout.getChildAt(0).getId(), chiledLinearLayout.getChildAt(1).getId());
                    }
                });
        }
    }

    // animation for show and hide fields labels
    private void runAnimation(int id1, int id2) {

        TextView tv = (TextView) mainView.findViewById(id1);
        FrameLayout bg = (FrameLayout) mainView.findViewById(id2);

        if (tv.getVisibility() == View.VISIBLE) {
            Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_sheet_fad_out);
            a.reset();
            tv.clearAnimation();
            tv.startAnimation(a);
            tv.setVisibility(View.GONE);
            bg.setBackgroundResource(R.drawable.ic_background_gray);
        } else {
            Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_sheet_fad_in);
            a.reset();
            tv.clearAnimation();
            tv.startAnimation(a);
            tv.setVisibility(View.VISIBLE);
            bg.setBackgroundResource(R.drawable.ic_background_gray_down);

        }

    }

    private void updateProgress() {
        BaseClient.getBaseClient().create(Api.class)
                .getRemainingOrders(SharedHelper.getKey(getActivity(), LoginActivity.TOKEN))
                .enqueue(new Callback<RemainingOrdersResponse>() {
                    @Override
                    public void onResponse(Call<RemainingOrdersResponse> call, Response<RemainingOrdersResponse> response) {
                        if (response.body() != null) {

                            if (!firstOrder) {
                                firstOrder = true;
                                firstRemaining = response.body().getData();
                                mProgressBar4.setMax(firstRemaining);
                            }

                            int b = firstRemaining - response.body().getData();
                            mProgressBar4.setProgress(b);
                            String remaining = getString(R.string.remaining) + " ( " + NumberFormat.getNumberInstance(Locale.US).format(response.body().getData()) + " ) " + getString(R.string.order);
                            present.setText(remaining);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            if (sharedPreferences.getBoolean("autoNotifiction", false))
                                createNotification(String.valueOf(firstRemaining - b));

                        }
                    }

                    @Override
                    public void onFailure(Call<RemainingOrdersResponse> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    public void createNotification(String first) {

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;

            channel = new NotificationChannel("5", "eslam", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("5");
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "5")
                    .setSmallIcon(getActivity().getApplicationInfo().icon)
                    .setContentTitle("orders")
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .addAction(R.drawable.ic_call_end_red, getResources().getString(R.string.start_work),
                            PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(),
                                    NewOrderActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0))
                    .setContentText(getString(R.string.remaining) + " " + first + " " + getString(R.string.order))
                    .setSmallIcon(R.drawable.ic_launcher);


            notificationManager.notify(5, builder.build());

        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                    .setSmallIcon(getActivity().getApplicationInfo().icon)
                    .setContentTitle("orders")
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .addAction(R.drawable.ic_call_end_red, getResources().getString(R.string.start_work),
                            PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(),
                                    OrderActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0))
                    .setContentText(getString(R.string.remaining) + " " + first + " " + getString(R.string.order))
                    .setSmallIcon(R.drawable.ic_launcher);


            notificationManager.notify(5, builder.build());
        }


    }

}
