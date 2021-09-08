package com.example.secure_workspace;

public class Device_Info {

    public String Device_Name;
    public String Device_Address;
    public String Received_Data;
    public String Connected;

    Device_Info(){
        Device_Name = null;
        Device_Address = null;
        Received_Data = null;
        Connected = null;
    }

    Device_Info(String Device_Name, String Device_Address, String Received_Data, String Connected){
        this.Device_Name = Device_Name;
        this.Device_Address = Device_Address;
        this.Received_Data = Received_Data;
        this.Connected = Connected;
    }
}
