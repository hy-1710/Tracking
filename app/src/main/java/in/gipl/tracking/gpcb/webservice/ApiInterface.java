package in.gipl.tracking.gpcb.webservice;

import java.util.List;


import in.gipl.tracking.gpcb.database.IntervalData;
import in.gipl.tracking.gpcb.database.SyncPostData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {


  // @POST("/SyncTracklog")

  @Headers(
          {"Content-Type  : text/plain"
          }
  )

  @POST("SyncTracklogSyncTracklog")
  Call<List<SyncPostData>> SyncTrackLog(@Body String TrackLogs);

  @POST("SyncTracklog")
  Call<List<SyncPostData>> sendTrackLog( @Body  String TrackLogs);


  @GET("GetTrackingConfig")
    Call<List<IntervalData>> getTopRatedMovies();
}
