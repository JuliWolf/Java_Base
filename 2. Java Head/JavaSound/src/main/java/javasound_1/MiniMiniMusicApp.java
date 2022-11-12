package javasound_1;


import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.ShortBuffer;
import java.util.ArrayList;

//public class MiniMiniMusicApp {
//  public static void main (String[] args) {
//    MiniMiniMusicApp mini = new MiniMiniMusicApp();
//    if (args.length < 2) {
//      System.out.println("Не забудьте аргументы для инструмента и ноты");
//    } else {
//      int instrument = Integer.parseInt(args[0]);
//      int note = Integer.parseInt(args[1]);
//      mini.play(instrument, note);
//    }
//
//  }
//
//  public void play (int instrument, int note) {
//    try {
////    Получаем синтезатор и открываем его, чтобы начать использовать
//      Sequencer player = MidiSystem.getSequencer();
//      player.open();
//
//      Sequence seq = new Sequence(Sequence.PPQ, 4);
//
////    Запрашиваем трек у последовательности
//      Track track = seq.createTrack();
//
//      MidiEvent event = null;
//
////    Помещаем в трек Midi события
////    Создаем сообщение
//      ShortMessage first = new ShortMessage();
////    Помещаем в сообщение инструкцию
//      first.setMessage(192, 1, instrument, 0);
////    Используя сообщение, создаем новое событие
//      MidiEvent changeInstrument = new MidiEvent(first, 1);
////    Добавляем событие в трек
//      track.add(changeInstrument);
//
//      ShortMessage b = new ShortMessage();
//      b.setMessage(192, 1, note, 100);
//      MidiEvent noteOff = new MidiEvent(b, 16);
//      track.add(noteOff);
//
////    Передаем последовательность синтезатору
//      player.setSequence(seq);
//      player.setTempoInBPM(220);
//
////    Запускаем синтезатор
//      player.start();
//
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
//  }
//}
//
//// Добавляем makeEvent для создания звуков
//class MiniMiniMusicApp2 {
//  public static void main (String[] args) {
//    MiniMiniMusicApp2 mini = new MiniMiniMusicApp2();
//    if (args.length < 2) {
//      System.out.println("Не забудьте аргументы для инструмента и ноты");
//    } else {
//      int instrument = Integer.parseInt(args[0]);
//      int note = Integer.parseInt(args[1]);
//      mini.play(instrument, note);
//    }
//
//  }
//
//  public void play (int instrument, int note) {
//    try {
//      Sequencer player = MidiSystem.getSequencer();
//      player.open();
//
//      Sequence seq = new Sequence(Sequence.PPQ, 4);
//      Track track = seq.createTrack();
//
//      for (int i = 5; i < 61; i+=4) {
////      Вызываем новый метод, чтобы создать сообщение и событие
//        track.add(makeEvent(144, 1, i, 100, i));
//        track.add(makeEvent(144, 1, i, 100, i + 2));
//      }
//
//      player.setSequence(seq);
//      player.setTempoInBPM(220);
//      player.start();
//
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
//  }
//
//  public static MidiEvent makeEvent (int comd, int chan, int one, int two, int tick) {
//    MidiEvent event = null;
//
//    try {
//      ShortMessage a = new ShortMessage();
//      a.setMessage(comd, chan, one, two);
//      event = new MidiEvent(a, tick);
//    } catch (Exception e) {}
//    return event;
//  }
//}
//
//
//// Добавляем слушатель событий
//class MiniMiniMusicApp3 implements ControllerEventListener {
//  public static void main (String[] args) {
//    MiniMiniMusicApp3 mini = new MiniMiniMusicApp3();
//    if (args.length < 2) {
//      System.out.println("Не забудьте аргументы для инструмента и ноты");
//    } else {
//      int instrument = Integer.parseInt(args[0]);
//      int note = Integer.parseInt(args[1]);
//      mini.play(instrument, note);
//    }
//
//  }
//
//  public void play (int instrument, int note) {
//    try {
//      Sequencer player = MidiSystem.getSequencer();
//      player.open();
//
////    Регистрируем события синтезатором
//      int[] eventsIWant = {127};
//      player.addControllerEventListener(this, eventsIWant);
//
//      Sequence seq = new Sequence(Sequence.PPQ, 4);
//      Track track = seq.createTrack();
//
//      for (int i = 5; i < 61; i+=4) {
//        track.add(makeEvent(144, 1, i, 100, i));
//
////      Ловим ритм и собавляем наше собственное событие
//        track.add(makeEvent(176, 1, 127, 0, i));
//
//        track.add(makeEvent(144, 1, i, 100, i + 2));
//      }
//
//      player.setSequence(seq);
//      player.setTempoInBPM(220);
//      player.start();
//
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
//  }
//
////Метод обработки событи. При каждом получении события мы пишем в командной строке слово ля
//  public void controlChange (ShortMessage event) {
//    System.out.println("ля");
//  }
//
//  public static MidiEvent makeEvent (int comd, int chan, int one, int two, int tick) {
//    MidiEvent event = null;
//
//    try {
//      ShortMessage a = new ShortMessage();
//      a.setMessage(comd, chan, one, two);
//      event = new MidiEvent(a, tick);
//    } catch (Exception e) {}
//    return event;
//  }
//}
//
//
////Рисуем графику в такт музыке
//
//// Наследуемся от панели рисования
//class MiniMiniMusicApp4 {
//  static JFrame f = new JFrame("Мой первый музыкальный клип");
//  static MyDrawPanel ml;
//
//  public static void main (String[] args) {
//    MiniMiniMusicApp4 mini = new MiniMiniMusicApp4();
//    mini.go();
//  }
//
//  public void setUpGui(){
//    ml = new MyDrawPanel();
//    f.setContentPane(ml);
//    f.setBounds(30, 30, 300, 300);
//    f.setVisible(true);
//  }
//
//  public void go () {
//    setUpGui();
//
//    try {
//      Sequencer player = MidiSystem.getSequencer();
//      player.open();
//      player.addControllerEventListener(ml, new int[] {127});
//
//      Sequence seq = new Sequence(Sequence.PPQ, 4);
//      Track track = seq.createTrack();
//
//      int r = 0;
//      for (int i = 5; i < 61; i += 4) {
//        r = (int) ((Math.random() * 50) + 1);
//        track.add(makeEvent(144, 1, r, 100, i));
//        track.add(makeEvent(176, 1, 127, 0, i));
//        track.add(makeEvent(144, 1, r, 100, i + 2));
//      }
//
//      player.setSequence(seq);
//      player.start();
//      player.setTempoInBPM(120);
//
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
//  }
//
//  public static MidiEvent makeEvent (int comd, int chan, int one, int two, int tick) {
//    MidiEvent event = null;
//
//    try {
//      ShortMessage a = new ShortMessage();
//      a.setMessage(comd, chan, one, two);
//      event = new MidiEvent(a, tick);
//    } catch (Exception e) {}
//    return event;
//  }
//}
//
//class MyDrawPanel extends JPanel implements ControllerEventListener {
//  //Присваиваем флагу значение false и будем устанавливать true, когда получим событие
//  boolean msg = false;
//
//  public void controlChange (ShortMessage event) {
////  Когда получаем событие присваиваем флагу значение true
//    msg = true;
////  Вызываем repaint
//    repaint();
//  }
//
//  public void paintComponent (Graphics g) {
////  Вызываем событие только тогда, когда произошло событие ControllerEvent
//    if (msg) {
//      Graphics2D g2 = (Graphics2D) g;
//
//      int r = (int) (Math.random() * 250);
//      int gr = (int) (Math.random() * 250);
//      int b = (int) (Math.random() * 250);
//
//      g.setColor(new Color(r, gr, b));
//
//      int ht = (int) ((Math.random() * 120) + 10);
//      int width = (int) ((Math.random() * 120) + 10);
//      int x = (int) ((Math.random() * 40) + 10);
//      int y = (int) ((Math.random() * 40) + 10);
//      g.fillRect(x, y, ht, width);
//      msg = false;
//    }
//  }
//}


// Создаем приложение с визуалом
class BeatBox {
  JPanel mainPanel;
//Массив для хранение флагов
  ArrayList<JCheckBox> checkboxList;
  Sequencer sequencer;
  Sequence sequence;
  Track track;
  JFrame theFrame;

  String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
      "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
      "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Cong",
      "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
      "Open Hi Conga"};
  int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

  public static void main (String[] args) {
    new BeatBox().buildGUI();
  }

  public void buildGUI () {
    theFrame = new JFrame("Cyber BeatBox");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    BorderLayout layout = new BorderLayout();
    JPanel background = new JPanel(layout);
    background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    checkboxList = new ArrayList<JCheckBox>();
    Box buttonBox = new Box(BoxLayout.Y_AXIS);

    JButton start = new JButton("Start");
    start.addActionListener(new MyStartListener());
    buttonBox.add(start);

    JButton stop = new JButton("Stop");
    stop.addActionListener(new MyStopListener());
    buttonBox.add(stop);

    JButton upTempo = new JButton("Tempo Up");
    upTempo.addActionListener(new MyUpTempoListener());
    buttonBox.add(upTempo);

    JButton downTempo = new JButton("Tempo Down");
    downTempo.addActionListener(new MyDownTempoListener());
    buttonBox.add(downTempo);

    Box nameBox = new Box(BoxLayout.Y_AXIS);
    for(int i = 0; i < 16; i++) {
      nameBox.add(new Label(instrumentNames[i]));
    }

    background.add(BorderLayout.EAST, buttonBox);
    background.add(BorderLayout.WEST, nameBox);

    theFrame.getContentPane().add(background);

    GridLayout grid = new GridLayout(16, 16);
    grid.setVgap(1);
    grid.setVgap(2);
    mainPanel = new JPanel(grid);
    background.add(BorderLayout.CENTER, mainPanel);

    for(int i = 0; i < 256; i++) {
      JCheckBox c = new JCheckBox();
      c.setSelected(false);
      checkboxList.add(c);
      mainPanel.add(c);
    }

    setUpMidi();

    theFrame.setBounds(50, 50, 300, 300);
    theFrame.pack();
    theFrame.setVisible(true);
  }

  public void setUpMidi () {
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequence = new Sequence(Sequence.PPQ, 4);
      track = sequence.createTrack();
      sequencer.setTempoInBPM(120);
    }catch(Exception e ) {
      e.printStackTrace();
    }
  }

  public void buildTrackAndStart () {
    int[] trackList = null;

    sequence.deleteTrack(track);
    track = sequence.createTrack();

    for(int i = 0; i < 16; i++) {
      trackList = new int[16];

      int key = instruments[i];

      for(int j = 0; j < 16; j++) {
        JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
        if(jc.isSelected()) {
          trackList[j] = key;
        } else {
          trackList[j] = 0;
        }
      }

      makeTracks(trackList);
      track.add(makeEvent(176, 1, 127,0,16));
    }

    track.add(makeEvent(192,9,1,0,15));
    try {
      sequencer.setSequence(sequence);
      sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
      sequencer.start();
      sequencer.setTempoInBPM(120);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public class MyStartListener implements ActionListener {
    public void actionPerformed (ActionEvent a) {
      buildTrackAndStart();
    }
  }

  public class MyStopListener implements ActionListener {
    public void actionPerformed (ActionEvent a) {
      sequencer.stop();
    }
  }

  public class MyUpTempoListener implements ActionListener {
    public void actionPerformed (ActionEvent a) {
      float tempoFactor = sequencer.getTempoFactor();
      sequencer.setTempoFactor((float) (tempoFactor * 1.03));
    }
  }

  public class MyDownTempoListener implements ActionListener {
    public void actionPerformed (ActionEvent a) {
      float tempoFactor = sequencer.getTempoFactor();
      sequencer.setTempoFactor((float) (tempoFactor * .97));
    }
  }

  public class MySendListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      boolean[] checkboxState = new boolean[256];

        for(int i = 0; i < 256; i++) {
          JCheckBox check = (JCheckBox) checkboxList.get(i);
          if(check.isSelected()) {
            checkboxState[i] = true;
          }
        }

        try {
          FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
          ObjectOutputStream os = new ObjectOutputStream(fileStream);
          os.writeObject(checkboxState);
        }catch(Exception ex) {
          ex.printStackTrace();
        }
    }
  }

  public class MyReadListener implements ActionListener {
    public void actionPerformed (ActionEvent a) {
      boolean[] checkboxState = null;

      try {
        FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
        ObjectInputStream is = new ObjectInputStream(fileIn);
        checkboxState = (boolean[]) is.readObject();
      }catch(Exception ex) {
        ex.printStackTrace();
      }

      for (int i = 0; i < 256; i++) {
        JCheckBox check = (JCheckBox) checkboxList.get(i);
        if(checkboxState[i]) {
          check.setSelected(true);
        } else {
          check.setSelected(false);
        }
      }

      sequencer.stop();
      buildTrackAndStart();
    }
  }

  public void makeTracks (int[] list) {
    for(int i = 0; i < 16; i++) {
      int key = list[i];

      if (key != 0) {
        track.add(makeEvent(144,9,key, 100, i));
        track.add(makeEvent(128,9,key, 100, i+1));
      }
    }
  }

  public MidiEvent makeEvent (int comd, int chan, int one, int two, int tick) {
    MidiEvent event = null;

    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(comd, chan, one, two);
      event = new MidiEvent(a, tick);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return event;
  }
}