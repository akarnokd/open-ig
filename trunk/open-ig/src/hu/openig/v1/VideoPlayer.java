/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import hu.openig.ani.MovieSurface;
import hu.openig.ani.MovieSurface.ScalingMode;
import hu.openig.core.ImageInterpolation;
import hu.openig.sound.AudioThread;
import hu.openig.v1.ResourceLocator.ResourcePlace;

import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.GZIPInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * Video player for the new format.
 * @author karnok, 2009.09.26.
 * @version $Revision 1.0$
 */
public class VideoPlayer extends JFrame {
	/** */
	private static final long serialVersionUID = 2254173821215224641L;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The configuration. */
	protected Configuration config;
	/** The video model. */
	protected VideoModel videoModel;
	/** The video table. */
	protected JTable videoTable;
	/** The playback surface. */
	private MovieSurface surface;
	/** The playback position. */
	private JSlider position;
	/** The volume. */
	private JSlider volumeSlider;
	/** The position in time. */
	private JLabel positionTime;
	/** The subtitle. */
	private JLabel subtitle;
	/** Video worker. */
	private volatile Worker videoWorker;
	/** Audio worker. */
	private volatile AudioWorker audioWorker;
	/** Stop the playback. */
	protected volatile boolean stop;
	/** The current video. */
	protected VideoEntry currentVideo;
	/** The current fps value. */
	protected volatile double currentFps;
	/** The subtitle manager. */
	protected SubtitleManager subs;
	/** Synchronization point for audio-video playback. */
	protected CyclicBarrier barrier;
	/** Play button. */
	private ConfigButton btnPlay;
	/** Pause button. */
	private ConfigButton btnPause;
	/** Stop button. */
	private ConfigButton btnStop;
	/** The audio length. */
	protected volatile int audioLen;
	/**
	 * Video entry.
	 * @author karnok, 2009.09.26.
	 * @version $Revision 1.0$
	 */
	class VideoEntry {
		/** Name. */
		String name;
		/** Path. */
		String path;
		/** The video language. */
		String video;
		/** Available audio languages. */
		String audio;
		/** Available subtitles. */
		String subtitle;
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((audio == null) ? 0 : audio.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result
					+ ((subtitle == null) ? 0 : subtitle.hashCode());
			result = prime * result + ((video == null) ? 0 : video.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			VideoEntry other = (VideoEntry) obj;
			if (audio == null) {
				if (other.audio != null) {
					return false;
				}
			} else if (!audio.equals(other.audio)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (path == null) {
				if (other.path != null) {
					return false;
				}
			} else if (!path.equals(other.path)) {
				return false;
			}
			if (subtitle == null) {
				if (other.subtitle != null) {
					return false;
				}
			} else if (!subtitle.equals(other.subtitle)) {
				return false;
			}
			if (video == null) {
				if (other.video != null) {
					return false;
				}
			} else if (!video.equals(other.video)) {
				return false;
			}
			return true;
		}
	}
	/**
	 * Video table model.
	 * @author karnok, 2009.09.26.
	 * @version $Revision 1.0$
	 */
	class VideoModel extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 3860832368918760138L;
		/** The column names. */
		public String[] colNames = { "Name", "Path", "Audio", "Subtitles" };
		/** The rows. */
		public List<VideoEntry> rows = new ArrayList<VideoEntry>();
		@Override
		public int getColumnCount() {
			return colNames.length;
		}
		@Override
		public int getRowCount() {
			return rows.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			VideoEntry ve = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return ve.name;
			case 1:
				return ve.path;
			case 2:
				return ve.audio;
			case 3:
				return ve.subtitle;
			default:
				return null;
			}
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}
	}
	/**
	 * Scan resources.
	 */
	protected void scan() {
		rl.clear();
		rl.setContainers(config.containers);
		rl.scanResources();
		videoModel.rows.clear();
		Set<VideoEntry> result = new LinkedHashSet<VideoEntry>();
		Map<String, Map<String, ResourcePlace>> videos = rl.resourceMap.get(ResourceType.VIDEO);
		Map<String, Map<String, ResourcePlace>> audios = rl.resourceMap.get(ResourceType.AUDIO);
		
		for (Map.Entry<String, Map<String, ResourcePlace>> rpe : videos.entrySet()) {
			for (String s : rpe.getValue().keySet()) {
				int idx = s.lastIndexOf('/');
				String name = null;
				String path = null;
				if (idx >= 0) {
					name = s.substring(idx + 1);
					path = s.substring(0, idx);
				} else {
					name = s;
					path = "";
				}
				
				// look for audio and subtitle on the same language
				ResourcePlace audio = rl.getExactly(rpe.getKey(), s, ResourceType.AUDIO);
				ResourcePlace subtitle = rl.getExactly(rpe.getKey(), s, ResourceType.SUBTITLE);
				
				boolean found = false;
				
				if (audio != null || subtitle != null) {
					VideoEntry ve = new VideoEntry();
					ve.name = name;
					ve.path = path;
					ve.video = rpe.getKey();
					if (audio != null) {
						ve.audio = rpe.getKey();
					} else {
						ve.audio = "";
					}
					if (subtitle != null) {
						ve.subtitle = rpe.getKey();
					} else {
						ve.subtitle = "";
					}
					result.add(ve);
					found = true;
				} else {
					// generic video but language dependant audio or sub
					for (String audioLang : audios.keySet()) {
						if (audioLang.equals(rpe.getKey())) {
							continue;
						}
						audio = rl.getExactly(audioLang, s, ResourceType.AUDIO);
						subtitle = rl.getExactly(audioLang, s, ResourceType.SUBTITLE);
						if (audio != null || subtitle != null) {
							VideoEntry ve = new VideoEntry();
							ve.name = name;
							ve.path = path;
							ve.video = rpe.getKey();
							if (audio != null) {
								ve.audio = audioLang;
							} else {
								ve.audio = "";
							}
							if (subtitle != null) {
								ve.subtitle = audioLang;
							} else {
								ve.subtitle = "";
							}
							result.add(ve);
							found = true;
						}
					}
				}
				if (!found) {
					VideoEntry ve = new VideoEntry();
					ve.name = name;
					ve.path = path;
					ve.video = rpe.getKey();
					ve.audio = "";
					ve.subtitle = "";
					result.add(ve);
				}
			}
		}
		videoModel.rows.addAll(result);
		videoModel.fireTableDataChanged();
	}
	/**
	 * @param args arguments
	 */
	public static void main(String[] args) {
		Configuration config = new Configuration("open-ig-config.xml");
		Set<String> argset = new HashSet<String>(Arrays.asList(args));
		if (config.load() && !argset.contains("-config")) {
			if (config.disableD3D) {
				System.setProperty("sun.java2d.d3d", "false");
			}
			if (config.disableDirectDraw) {
				System.setProperty("sun.java2d.noddraw", "false");
			}
			if (config.disableOpenGL) {
				System.setProperty("sun.java2d.opengl", "false");
			}
			showMainWindow(config);
		} else {
			showConfigWindow(config);
		}
	}
	/** 
	 * Display the main window. 
	 * @param config the configuration
	 */
	static void showMainWindow(final Configuration config) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				VideoPlayer wp = new VideoPlayer(config);
				wp.setLocationRelativeTo(null);
				wp.setVisible(true);
			}
		});
	}
	/** 
	 * Display the main window. 
	 * @param config the configuration
	 */
	static void showConfigWindow(final Configuration config) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Setup wp = new Setup(config);
				wp.onRun.add(new Act() { public void act() { showMainWindow(config); } });
				wp.setLocationRelativeTo(null);
				wp.setVisible(true);
			}
		});
	}
	/**
	 * Constructor.
	 * @param config the configuration
	 */
	public VideoPlayer(Configuration config) {
		super("Open-IG Video player");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		rl = new ResourceLocator();
		this.config = config;
		Container c = getContentPane();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doExit();
			}
		});
		
		JPanel videoPanel = new JPanel();
		
		GroupLayout gl = new GroupLayout(videoPanel);
		videoPanel.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		surface = new MovieSurface();
		
		videoModel = new VideoModel();
		videoTable = new JTable(videoModel);
		videoTable.setAutoCreateRowSorter(true);
		JScrollPane sp = new JScrollPane(videoTable);
		videoTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doMouseClicked(e);
			}
		});
		
		videoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		videoTable.getRowSorter().setSortKeys(Arrays.asList(new SortKey(1, SortOrder.ASCENDING), new SortKey(0, SortOrder.ASCENDING)));
		
		JMenuBar menuBar = new JMenuBar();
		JMenu mnuFile = new JMenu("File");
		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.addActionListener(new Act() { public void act() { doExit(); } });
		
		JMenuItem mnuRescan = new JMenuItem("Rescan");
		mnuRescan.addActionListener(new Act() { public void act() { doRescan(); } });

		JMenuItem mnuSetup = new JMenuItem("Setup");
		mnuSetup.addActionListener(new Act() { public void act() { doSetup(); } });

		
		mnuFile.add(mnuRescan);
		mnuFile.add(mnuSetup);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileExit);
		menuBar.add(mnuFile);
		
		JMenu mnuView = new JMenu("View");
		menuBar.add(mnuView);
		
		JMenuItem keepAspect = createRadioItem("Keep aspect", new Act() { public void act() { setScale(ScalingMode.KEEP_ASPECT); } });
		JMenuItem scale = createRadioItem("Scale to window", new Act() { public void act() { setScale(ScalingMode.WINDOW_SIZE); } });
		JMenuItem noScale = createRadioItem("Original size", new Act() { public void act() { setScale(ScalingMode.NONE); } });
		keepAspect.setSelected(true);

		
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(keepAspect);
		bg1.add(scale);
		bg1.add(noScale);
		
		mnuView.add(keepAspect);
		mnuView.add(scale);
		mnuView.add(noScale);
		mnuView.addSeparator();
		
		JMenuItem interDefault = createRadioItem("Default Interpolation", new Act() { public void act() { setInterpolation(ImageInterpolation.NONE); } });
		JMenuItem interLinear = createRadioItem("Linear Interpolation", new Act() { public void act() { setInterpolation(ImageInterpolation.NEIGHBOR); } });
		JMenuItem interBilinear = createRadioItem("Bilinear Interpolation", new Act() { public void act() { setInterpolation(ImageInterpolation.BILINEAR); } });
		JMenuItem interBicubic = createRadioItem("Bicubic Interpolation", new Act() { public void act() { setInterpolation(ImageInterpolation.BICUBIC); } });
		
		interDefault.setSelected(true);
		
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(interDefault);
		bg2.add(interLinear);
		bg2.add(interBilinear);
		bg2.add(interBicubic);
		
		mnuView.add(interDefault);
		mnuView.add(interLinear);
		mnuView.add(interBilinear);
		mnuView.add(interBicubic);
		
		
		setJMenuBar(menuBar);
		
		btnPlay = new ConfigButton("Play");
		btnPlay.addActionListener(new Act() { public void act() { doPlay(); } });
		btnPause = new ConfigButton("Pause");
		btnPause.addActionListener(new Act() { public void act() { doPause(); } });
		btnStop = new ConfigButton("Stop");
		btnStop.addActionListener(new Act() { public void act() { doStop(); } });
//		btnStop.setEnabled(false);
		
		position = new JSlider(0, 0);
		position.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setPositionLabel(currentFps, position.getValue());
			}
		});
		
		volumeSlider = new JSlider(0, 100);
		volumeSlider.addChangeListener(new ChangeListener() { 
			@Override
			public void stateChanged(ChangeEvent e) {
				doVolume();
			}
		});
		volumeSlider.setValue(config.videoVolume);
		
		positionTime = new JLabel();
		subtitle = new JLabel();
		
		JLabel volumeLabel = new JLabel("Volume:");
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(surface, 0, 320, Short.MAX_VALUE)
			.addComponent(subtitle, 0, 320, Short.MAX_VALUE)
			.addComponent(position, 0, 320, Short.MAX_VALUE)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(btnPlay)
//				.addComponent(btnPause)
				.addComponent(btnStop)
				.addComponent(positionTime, 150, 150, 150)
				.addComponent(volumeLabel)
				.addComponent(volumeSlider, 100, 100, 100)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(surface, 0, 240, Short.MAX_VALUE)
			.addComponent(subtitle, 50, 50, 50)
			.addComponent(position, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(btnPlay)
	//				.addComponent(btnPause)
					.addComponent(btnStop)
					.addComponent(positionTime)
					.addComponent(volumeLabel)
				)
				.addComponent(volumeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		
		JSplitPane spp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		spp.setDividerLocation(250);
		spp.setOneTouchExpandable(true);
		
		spp.setLeftComponent(sp);
		spp.setRightComponent(videoPanel);
		
		c.add(spp);
		
		pack();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scan();
			}
		});
	}
	/**
	 * Show setup window.
	 */
	protected void doSetup() {
		Setup s = new Setup(config);
		s.setLocationRelativeTo(this);
		s.setVisible(true);
	}
	/**
	 * Set scaling mode.
	 * @param mode the scaling mode
	 */
	protected void setScale(ScalingMode mode) {
		surface.setScalingMode(mode);
	}
	/**
	 * Set interpolation.
	 * @param ip the interpolation
	 */
	protected void setInterpolation(ImageInterpolation ip) {
		surface.setInterpolation(ip);
	}
	/**
	 * Create a menu item.
	 * @param title the title
	 * @param action the action or null
	 * @return the menu item
	 */
	protected JMenuItem createRadioItem(String title, Act action) {
		JMenuItem result = new JRadioButtonMenuItem(title);
		if (action != null) {
			result.addActionListener(action);
		}
		return result;
	}
	/** Perform exit. */
	protected void doExit() {
		doStop();
		dispose();
		rl.close();
	}
	/**
	 * Perform a rescan.
	 */
	protected void doRescan() {
		scan();
	}
	/**
	 * On volume change.
	 */
	protected void doVolume() {
		AudioWorker aw = audioWorker;
		if (aw != null) {
			aw.setVolume(volumeSlider.getValue());
		}
	}
	/** Start playback. */
	protected void doPlay() {
		try {
			Worker w1 = videoWorker;
			AudioWorker w2 = audioWorker;
			if (w2 != null) {
				stop = true;
				w2.stopPlayback();
				w2.join();
			}
			if (w1 != null) {
				stop = true;
				w1.interrupt();
				w1.join();
			}
			stop = false;
			if (currentVideo != null) {
//				btnPlay.setEnabled(false);
//				btnStop.setEnabled(true);
				// if at the end, rewind
				if (position.getValue() >= position.getMaximum() - 1) {
					position.setValue(0);
				}
				playVideo(currentVideo.video, currentVideo.path, currentVideo.name, currentVideo.audio, currentVideo.subtitle);
			}
		} catch (InterruptedException ex) {
			config.error(ex);
		}
	}
	/** Stop playback. */
	protected void doStop() {
//		btnStop.setEnabled(false);
//		btnPlay.setEnabled(true);
		if (videoWorker != null) {
			videoWorker.interrupt();
			stop = true;
		}
		if (audioWorker != null) {
			audioWorker.stopPlayback();
		}
	}
	/** Pause playback now. */
	protected void doPause() {
	}
	/** Worker with swing output. */
	public abstract class Worker extends Thread {
		/** The work. */
		protected abstract void work();
		/** Done. */
		protected void done() {
			
		}
		@Override
		public void run() {
			try {
				work();
			} finally {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						done();
					}
				});
			}
		}
	}
	/** 
	 * Do mouse clicked.
	 * @param e the event 
	 */
	protected void doMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int idx = videoTable.getSelectedRow();
			idx = videoTable.convertRowIndexToModel(idx);
			VideoEntry ve = videoModel.rows.get(idx);
			currentVideo = ve;
			position.setValue(0);
			doPlay();
		}
	}
	/**
	 * Play the video file.
	 * @param language the video language
	 * @param path the path
	 * @param name the filename
	 * @param audio the audio language
	 * @param subtitle the subtitle language
	 */
	public void playVideo(String language, String path, String name, String audio, String subtitle) {
		final ResourcePlace video = rl.get(language, path + "/" + name, ResourceType.VIDEO);
		if (video == null) {
			return;
		}
		if (subtitle != null && !subtitle.isEmpty()) {
			final ResourcePlace sub = rl.get(subtitle, path + "/" + name, ResourceType.SUBTITLE);
			subs = new SubtitleManager(sub.open());
		} else {
			subs = null;
		}
		final int skip = position.getValue();
		videoWorker = new Worker() {
			@Override
			protected void work() {
				decodeVideo(video, skip);
			}
			@Override
			protected void done() {
			}
		};
		if (audio != null && !audio.isEmpty()) {
			final ResourcePlace sound = rl.get(audio, path + "/" + name, ResourceType.AUDIO);
			audioWorker = createAudioWorker(sound);
			audioWorker.start();
			barrier = new CyclicBarrier(2);
		} else {
			audioLen = 0;
			barrier = new CyclicBarrier(1);
		}
		videoWorker.start();
		
	}
	/**
	 * Upscale the 8 bit signed values to 16 bit signed values.
	 * @param data the data to upscale
	 * @return the upscaled data
	 */
	public static short[] upscale8To16AndSignify(byte[] data) {
		short[] result = new short[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (short)(((data[i] & 0xFF) - 128) * 256);
		}
		return result;
	}
	/**
	 * The audio worker class.
	 * @author karnok, 2009.09.26.
	 * @version $Revision 1.0$
	 */
	class AudioWorker extends Worker {
		/** The sound clip. */
		private SourceDataLine clip;
		/** The audio resource. */
		private ResourcePlace audio;
		/** The number of audio samples to skip. */
		private int skip;
		/** The initial volume. */
		public int initialVolume = 100;
		/**
		 * Set audio volume.
		 * @param volume the volume
		 */
		public void setVolume(int volume) {
			if (clip != null) {
				FloatControl fc = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
				double minLinear = Math.pow(10, fc.getMinimum() / 20);
				double maxLinear = Math.pow(10, fc.getMaximum() / 20);
				fc.setValue((float)(20 * Math.log10(minLinear + volume * (maxLinear - minLinear) / 100)));
			}
		}
		/**
		 * Constructor.
		 * @param audio the audio resource
		 * @param skip the samples to skip
		 */
		public AudioWorker(ResourcePlace audio, int skip) {
			this.audio = audio;
			this.skip = skip;
		}
		/**
		 * Stop the playback.
		 */
		public void stopPlayback() {
			clip.stop();
			clip.close();
		}
		@Override
		protected void work() {
			byte[] buffer2 = null;
			try {
				AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(
						audio.open(), 256 * 1024));
				try {
					byte[] buffer = new byte[in.available()];
					in.read(buffer);
					buffer2 = AudioThread.split16To8(AudioThread.movingAverage(upscale8To16AndSignify(buffer), config.videoFilter));
					try {
						AudioFormat streamFormat = new AudioFormat(22050, 16, 1, true, false);
						DataLine.Info clipInfo = new DataLine.Info(SourceDataLine.class, streamFormat);
						clip = (SourceDataLine) AudioSystem.getLine(clipInfo);
						clip.open();
						setVolume(initialVolume);
						audioLen = buffer.length;
						try {
							barrier.await();
						} catch (InterruptedException ex) {
							
						} catch (BrokenBarrierException ex) {
							
						}
						clip.start();
						if (skip * 2 < buffer2.length) {
							clip.write(buffer2, skip * 2, buffer2.length - skip * 2);
						}
						clip.drain();
						clip.stop();
						clip.close();
					} catch (LineUnavailableException ex) {
						config.error(ex);
					}
				} finally {
					in.close();
				}
			} catch (UnsupportedAudioFileException ex) {
				config.error(ex);
			} catch (IOException ex) {
				config.error(ex);
			}
		}
	}
	/**
	 * Play the given audio file.
	 * @param audio the audio resource
	 * @return the audio worker
	 */
	public AudioWorker createAudioWorker(ResourcePlace audio) {
		int skip = 0;
		if (currentFps > 0) {
			skip = (int)(position.getValue() * 22050 / currentFps);
		}
		AudioWorker aw = new AudioWorker(audio, skip);
		aw.initialVolume = volumeSlider.getValue();
		return aw;
	}
	/**
	 * Decode the video.
	 * @param video the video location
	 * @param skipFrames the numer of frames to skip
	 */
	public void decodeVideo(ResourcePlace video, int skipFrames) {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 1024 * 1024), 1024 * 1024));
			try {
				int w = Integer.reverseBytes(in.readInt());
				int h = Integer.reverseBytes(in.readInt());
				final int frames = Integer.reverseBytes(in.readInt());
				double fps = Integer.reverseBytes(in.readInt()) / 1000.0;
				currentFps = fps;
				
				surface.init(w, h);
				
				int[] palette = new int[256];
				byte[] bytebuffer = new byte[w * h];
				int[] currentImage = new int[w * h];
				int frameCount = 0;
				long starttime = 0;
				int frames2 = frames;
				while (!stop) {
					int c = in.read();
					if (c < 0 || c == 'X') {
						break;
					} else
					if (c == 'P') {
						int len = in.read();
						for (int j = 0; j < len; j++) {
							int r = in.read() & 0xFF;
							int g = in.read() & 0xFF;
							int b = in.read() & 0xFF;
							palette[j] = 0xFF000000 | (r << 16) | (g << 8) | b;
						}
					} else
					if (c == 'I') {
						in.read(bytebuffer);
						for (int i = 0; i < bytebuffer.length; i++) {
							int c0 = palette[bytebuffer[i] & 0xFF];
							if (c0 != 0) {
								currentImage[i] = c0;
							}
						}
						if (frameCount >= skipFrames) {
							if (frameCount == skipFrames) {
								try {
									barrier.await();
								} catch (InterruptedException ex) {
									
								} catch (BrokenBarrierException ex) {
									
								}
								frames2 = (int)Math.ceil(audioLen * fps / 22050.0);
								setMaximumFrame(Math.max(frames, frames2));
								starttime = System.nanoTime();
							}
							surface.getBackbuffer().setRGB(0, 0, w, h, currentImage, 0, w);
							surface.swap();
							setPosition(fps, frameCount);
							// wait the frame/sec
							starttime += (1000000000.0 / fps);
			       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
						}
		       			frameCount++;
					}
				}
				// continue to emit reposition events
				if (frames2 > frames && !stop) {
					for (int i = frames; i < frames2 && !stop; i++) {
						setPosition(fps, i);
						// wait the frame/sec
						starttime += (1000000000.0 / fps);
		       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
					}
				}
			} finally {
				try { in.close(); } catch (IOException ex) { config.error(ex); }
			}
		} catch (IOException ex) {
			config.error(ex);
		}
	}
	/**
	 * Set the maximum frame.
	 * @param frames number of frames
	 */
	void setMaximumFrame(final int frames) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					position.setMaximum(frames);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Set position.
	 * @param fps frames per second
	 * @param position the position
	 */
	void setPosition(final double fps, final int position) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setPositionLabel(fps, position);
				VideoPlayer.this.position.setValue(position);
			}
		});
	}
	/**
	 * Set the position label.
	 * @param fps the frames per second
	 * @param position the position
	 */
	private void setPositionLabel(final double fps, final int position) {
		double time = position / fps;
		int mins = ((int)time) / 60;
		int secs = ((int)time) % 60;
		int msecs = ((int)(time * 1000) % 1000);
		positionTime.setText(String.format("%d | %02d:%02d.%03d", position, mins, secs, msecs));
		if (subs != null) {
			String s = subs.get((long)(time * 1000));
			if (s != null) {
				subtitle.setText("<html>" + s);
			} else {
				subtitle.setText("");
			}
		}
	}
}
