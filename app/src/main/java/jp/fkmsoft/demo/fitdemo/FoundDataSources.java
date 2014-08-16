package jp.fkmsoft.demo.fitdemo;

import com.google.android.gms.fitness.DataSource;
import com.google.android.gms.fitness.DataSourceListener;
import com.google.android.gms.fitness.DataType;

import java.util.List;

/**
 * Just a bean class
 */
public class FoundDataSources {
    public int mDataSourceType;
    public DataType mDataType;
    public List<DataSource> mDataSources;

    public DataSourceListener mDataSourceListener;
}
