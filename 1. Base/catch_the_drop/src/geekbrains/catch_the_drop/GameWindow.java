package geekbrains.catch_the_drop;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class GameWindow extends JFrame {

  private static GameWindow game_window;

  // Объявляем переменные для работы с картинками
  private static Image drop;
  private static Image game_over;
  private static Image background;

  // Значение x верхнего левого угла капли
  private static float drop_left = 200;

  // Значение y верхнего левого угла капли
  private static float drop_top = -100;

  // long - для очень больших чисел
  private static long last_frame_time;

  // Скорость капли
  private static float drop_v = 200;

  // Очки
  private static int score = 0;

  // обрабатываем исключение, которое может выкинуть ImageIO
  public static void main(String[] args) throws IOException {
    // Загружаем картинки
    drop = ImageIO.read(GameWindow.class.getResourceAsStream("drop.png"));
    game_over = ImageIO.read(GameWindow.class.getResourceAsStream("game_over.png"));
    background = ImageIO.read(GameWindow.class.getResourceAsStream("background.png"));

    // Инициализируем объект GameWindow
    game_window = new GameWindow();

    // При зкарытии окна, приложение будет завершаться
    game_window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    // устанавливаем начальное положение окна
    game_window.setLocation(1000, 100);

    // Устанавливаем размеры окна
    game_window.setSize(906, 478);

    // Запрещаем изменение размера окна
    game_window.setResizable(false);

    // Текущее время в наносекундах
    last_frame_time = System.nanoTime();

    // Создаем объект типа  GameField
    GameField game_field = new GameField();

    // Создаем обработчик клика
    game_field.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        //  Определяем нижнюю и правую границы капли
        float drop_right = drop_left + drop.getWidth(null);
        float drop_bottom = drop_top + drop.getHeight(null);

        boolean is_drop = x >= drop_left && x <= drop_right && y >= drop_top && y <= drop_bottom;

        if (is_drop) {
          drop_top = -100;
          drop_left = (int) (Math.random() * (game_field.getWidth() - drop.getWidth(null)));
          drop_v = drop_v + 20;
          score++;
          // Выводим счетчик вы заголовок окна
          game_window.setTitle("Score: +" + score);
        }
      }
    });

    //  Добавляем объект game_field внутрь game_window
    game_window.add(game_field);

    // Делаем окно видимым
    game_window.setVisible(true);
  }

  private static void onRepaint (Graphics g) {
//    // Рисуем овал шириной 200 и высотой 100, в точке y10 x10
//    g.fillOval(10, 10, 200, 100);
//
//    // Рисуем линию из точки 200/200 в точку 400/300
//    g.drawLine(200,200, 400, 300);

    long current_time = System.nanoTime();

    //  считаем разницу во времени и переводим в секунды
    float delta_time = (current_time - last_frame_time) * 0.000000001f;

    last_frame_time = current_time;

    // Отрисовываем картинки
    // отрисовываем background с точки 0/0
    g.drawImage(background, 0, 0, null);

    drop_top = drop_top + drop_v * delta_time;

    g.drawImage(drop, (int) drop_left, (int) drop_top, null);

    // game_window.getHeight() Высота окна
    if (drop_top > game_window.getHeight()) {
      // отрисовываем game_over в точке 280/120
      g.drawImage(game_over, 280, 120, null);
    }
  }

  private static class GameField extends JPanel {
    // Динамическое замещение метода
    // Переписываем логику унаследованного метода
    @Override
    protected void paintComponent (Graphics g) {
      // Обращаемся к унаследованному от JPanel методу
      super.paintComponent(g);

      onRepaint(g);

      //  Запускаем частую перерисовку
      repaint();
    }
  }
}
