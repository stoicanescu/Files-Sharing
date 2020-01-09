package com.example.filessharing;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;

class NsdHelper {

    private MainActivity mActivity;

    private String serviceName;
    private ArrayList<String> servicesArray = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private ArrayList<Device> devices = new ArrayList<Device>();

    private boolean registrationStarted = false;
    private boolean discoveryStarted = false;

    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String TAG = "NsdHelper";

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener registrationListener;
    private NsdManager.DiscoveryListener discoveryListener;


    private InetAddress ip_receiver_device = null;
    private int port_receiver_device = 0;
    private String connected_device = null;

    NsdHelper(MainActivity mActivity, String serviceName) {
        this.serviceName = serviceName;
        this.mActivity = mActivity;
        _configAdapter();
    }

    private void _configAdapter() {
        adapter = new ArrayAdapter<String>(mActivity, R.layout.list_view, servicesArray);
        mActivity.getList_view().setAdapter(adapter);
    }

    void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                String serviceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "registration info: success");
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                Log.d(TAG, "registration info: failure");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.d(TAG, "registration info: unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }

    void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success: ");
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + service.getServiceName());
                } else {
                    Log.d(TAG, "Different machine: " + service.getServiceName());
                    mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            // Called when the resolve fails. Use the error code to debug.
                            Log.e(TAG, "Resolve failed " + errorCode);
                            Log.e(TAG, "service = " + serviceInfo);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.d(TAG, "Resolve Succeeded. " + serviceInfo);
                            devices.add(new Device(serviceInfo.getServiceName(), serviceInfo.getHost(), serviceInfo.getPort()));
                            onDeviceClick();
                        }
                    });
                    if(!servicesArray.contains(service.getServiceName()))
                        servicesArray.add(service.getServiceName());
                    handleServicesArrayChange();
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
                servicesArray.remove(service.getServiceName());
                for(Device dev: devices) {
                    if(dev.getDeviceName().equals(service.getServiceName())) {
                        devices.remove(dev);
                        if(connected_device != null && connected_device.equals(service.getServiceName())) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mActivity.getConnectedTo_view().setText("");
                                    mActivity.getConnectedTo_view().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                    Toast.makeText(mActivity.getApplicationContext(), "Disconnected from: " + connected_device, Toast.LENGTH_SHORT).show();
                                    connected_device = null;
                                }
                            });
                        }
                        break;
                    }
                }
                handleServicesArrayChange();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    void startDiscoverServices() {
        if(!discoveryStarted) {
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }
        else {
            mNsdManager.stopServiceDiscovery(discoveryListener);
            initializeDiscoveryListener();
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }
        discoveryStarted = true;
    }

    void startRegisterService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) mActivity.getSystemService(Context.NSD_SERVICE);

        assert mNsdManager != null;
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        registrationStarted = true;
    }

    private void handleServicesArrayChange() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void onDeviceClick() {
        mActivity.getList_view().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                for(Device dev: devices) {
                    if(dev.getDeviceName().equals(servicesArray.get(position))) {
                        ip_receiver_device  = dev.getHostAddress();
                        port_receiver_device = dev.getHostPort();
                        connected_device = dev.getDeviceName();
                    }
                }
                if(ip_receiver_device != null) {
                    mActivity.getConnectedTo_view().setText("Connected to: " + connected_device);
                    mActivity.getConnectedTo_view().setCompoundDrawablesWithIntrinsicBounds(R.drawable.connected_to_check, 0, 0, 0);
                }
                else
                    Toast.makeText(mActivity.getApplicationContext(), "Connection failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    InetAddress getIp_receiver_device() {
        return ip_receiver_device;
    }

    int getPort_receiver_device() {
        return port_receiver_device;
    }

    void tearDown() {
        servicesArray.clear();
        handleServicesArrayChange();

        if(registrationStarted) {
            mNsdManager.unregisterService(registrationListener);
            registrationStarted = false;
        }
        if(discoveryStarted) {
            mNsdManager.stopServiceDiscovery(discoveryListener);
            discoveryStarted = false;
        }
        mActivity.getConnectedTo_view().setText("");
        mActivity.getConnectedTo_view().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        connected_device = null;
    }
}