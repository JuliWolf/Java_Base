package Concurrency;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * @author JuliWolf
 * @date 03.08.2023
 */
public class FutureRenderer {
//  private final ExecutorService executor = new ExecutorService();
//
//  void renderPage (CharSequence source) {
//    final List<ImageInfo> imageinfos = scanForImageInfo(source);
//
//    Callable<List<ImageData>> task = new Callable<List<ImageData>>() {
//      @Override
//      public List<ImageData> call() {
//        List<ImageData> result = new ArrayList<ImageData>();
//
//        for (ImageInfo imageInfo: imageinfos) {
//          result.add(imageInfo.downloadImage());
//        }
//
//        return result;
//      }
//    };
//
//    Future<List<ImageData>> future = executor.submit(task);
//    renderText(source);
//
//    try {
//      List<ImageData> imageData = future.get();
//      for (ImageData data: imageData) {
//        renderImage(data);
//      }
//    } catch (InterruptedException e) {
//      // Переподтвердить статус прерванности потока
//      Thread.currentThread().interrupt();
//      // Нам не нужен результат, поэтому отменить задачу
//      future.cancel(true);
//    } catch (ExecutionException e) {
//      throw launderThrowable(e.getCause());
//    }
//  }


}
