/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.ConfigButton;
import hu.openig.core.ImageInterpolation;
import hu.openig.core.MovieSurface;
import hu.openig.core.MovieSurface.ScalingMode;
import hu.openig.core.ResourceType;
import hu.openig.model.Configuration;
import hu.openig.model.ResourceLocator;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.screen.SubtitleManager;
import hu.openig.sound.AudioThread;
import hu.openig.utils.Exceptions;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * Video player for the new format.
 * @author akarnokd, 2009.09.26.
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
	 * @author akarnokd, 2009.09.26.
	 */
	static class VideoEntry {
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
	 * @author akarnokd, 2009.09.26.
	 */
	static class VideoModel extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 3860832368918760138L;
		/** The column names. */
		public String[] colNames = { "Name", "Path", "Audio", "Subtitles" };
		/** The rows. */
		public List<VideoEntry> rows = new ArrayList<>();
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
		rl = config.newResourceLocator();
		videoModel.rows.clear();
		Set<VideoEntry> result = new LinkedHashSet<>();
		Map<String, Map<String, ResourcePlace>> videos = rl.resourceMap.get(ResourceType.VIDEO);
		Map<String, Map<String, ResourcePlace>> audios = rl.resourceMap.get(ResourceType.AUDIO);
		
		for (Map.Entry<String, Map<String, ResourcePlace>> rpe : videos.entrySet()) {
			for (String s : rpe.getValue().keySet()) {
				int idx = s.lastIndexOf('/');
				String name;
				String path;
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
		if (config.load()) {
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
	 * Constructor.
	 * @param config the configuration
	 */
	public VideoPlayer(Configuration config) {
		super("Open-IG Video player");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.config = config;
		this.rl = config.newResourceLocator();
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
		mnuFileExit.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			doExit(); 
		} });
		
		JMenuItem mnuRescan = new JMenuItem("Rescan");
		mnuRescan.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			doRescan(); 
		} });

		JMenuItem mnuExport = new JMenuItem("Export...");
		mnuExport.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			doExport(); 
		} });
		
		mnuFile.add(mnuRescan);
		mnuFile.addSeparator();
		mnuFile.add(mnuExport);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileExit);
		menuBar.add(mnuFile);
		
		JMenu mnuView = new JMenu("View");
		menuBar.add(mnuView);
		
		JMenuItem keepAspect = createRadioItem("Keep aspect", new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			setScale(ScalingMode.KEEP_ASPECT); 
		} });
		JMenuItem scale = createRadioItem("Scale to window", new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			setScale(ScalingMode.WINDOW_SIZE); 
		} });
		JMenuItem noScale = createRadioItem("Original size", new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			setScale(ScalingMode.NONE); 
		} });
		keepAspect.setSelected(true);

		
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(keepAspect);
		bg1.add(scale);
		bg1.add(noScale);
		
		mnuView.add(keepAspect);
		mnuView.add(scale);
		mnuView.add(noScale);
		mnuView.addSeparator();
		
		JMenuItem interDefault = createRadioItem("Default Interpolation", new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
			setInterpolation(ImageInterpolation.NONE); 
		} });
		JMenuItem interLinear = createRadioItem("Linear Interpolation", new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			setInterpolation(ImageInterpolation.NEIGHBOR); 
		} });
		JMenuItem interBilinear = createRadioItem("Bilinear Interpolation", new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			setInterpolation(ImageInterpolation.BILINEAR); 
		} });
		JMenuItem interBicubic = createRadioItem("Bicubic Interpolation", new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			setInterpolation(ImageInterpolation.BICUBIC); 
		} });
		
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
		btnPlay.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			doPlay(); 
		} });
		btnPause = new ConfigButton("Pause");
		btnPause.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			doPause(); 
		} });
		btnStop = new ConfigButton("Stop");
		btnStop.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { 
			doStop(); 
		} });
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
	protected JMenuItem createRadioItem(String title, ActionListener action) {
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
			Exceptions.add(ex);
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
		final ResourcePlace video = rl.getExactly(language, path + "/" + name, ResourceType.VIDEO);
		if (video == null) {
			return;
		}
		if (subtitle != null && !subtitle.isEmpty()) {
			final ResourcePlace sub = rl.getExactly(subtitle, path + "/" + name, ResourceType.SUBTITLE);
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
			final ResourcePlace sound = rl.getExactly(audio, path + "/" + name, ResourceType.AUDIO);
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
	 * @author akarnokd, 2009.09.26.
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
				AudioThread.setVolume(clip, volume);
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
            if (clip != null) {
                clip.stop();
                clip.close();
            }
		}
		@Override
		protected void work() {
			try (AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(
						audio.open(), 256 * 1024))) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				try {
					AudioFormat af = in.getFormat();
					byte[] buffer2;
					if (af.getSampleSizeInBits() == 8) {
						if (af.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
							for (int i = 0; i < buffer.length; i++) {
								buffer[i] = (byte)((buffer[i] & 0xFF) - 128);
							}
						}
						buffer2 = AudioThread.convert8To16(buffer);
						af = new AudioFormat(af.getFrameRate(), 16, af.getChannels(), true, false);
					} else {
						buffer2 = buffer;
					}
					DataLine.Info clipInfo = new DataLine.Info(SourceDataLine.class, af);
					clip = (SourceDataLine) AudioSystem.getLine(clipInfo);
					clip.open();
					setVolume(initialVolume);
					audioLen = buffer.length / 2;
					try {
						barrier.await();
					} catch (InterruptedException | BrokenBarrierException ex) {
                                            // ignored
					}
					clip.start();
					if (skip * 2 < buffer2.length) {
						clip.write(buffer2, skip * 2, buffer2.length - skip * 2);
					}
					clip.drain();
					clip.close();
				} catch (LineUnavailableException ex) {
					Exceptions.add(ex);
				}
			} catch (UnsupportedAudioFileException | IOException ex) {
				Exceptions.add(ex);
			}
		}
	}
	/**
	 * Play the given audio file.
	 * @param audio the audio resource
	 * @return the audio worker
	 */
	AudioWorker createAudioWorker(ResourcePlace audio) {
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
	 * @param skipFrames the number of frames to skip
	 */
	public void decodeVideo(ResourcePlace video, int skipFrames) {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 1024 * 1024), 1024 * 1024))) {
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
			long startTime = 0;
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
					in.readFully(bytebuffer);
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
							} catch (InterruptedException | BrokenBarrierException ex) {
								
							}
							frames2 = (int)Math.ceil(audioLen * fps / 22050.0);
							setMaximumFrame(Math.max(frames, frames2));
							startTime = System.nanoTime();
						}
						surface.getBackbuffer().setRGB(0, 0, w, h, currentImage, 0, w);
						surface.swap();
						setPosition(fps, frameCount);
						// wait the frame/sec
						startTime += (1000000000.0 / fps);
		       			LockSupport.parkNanos((Math.max(0, startTime - System.nanoTime())));
					}
	       			frameCount++;
				}
			}
			// continue to emit reposition events
			if (frames2 > frames && !stop) {
				for (int i = frames; i < frames2 && !stop; i++) {
					setPosition(fps, i);
					// wait the frame/sec
					startTime += (1000000000.0 / fps);
	       			LockSupport.parkNanos((Math.max(0, startTime - System.nanoTime())));
				}
			}
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/** The last known export dir. */
	File lastDir;
	/** The export record. */
	static class AVExport {
		/** Video location. */
		ResourcePlace video;
		/** Audio location. */
		ResourcePlace audio;
		/** File naming. */
		String naming;
	}
	/** Export the selected videos as PNG images. */
	void doExport() {
		JFileChooser jfc = new JFileChooser(lastDir != null ? lastDir : new File("."));
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDir = jfc.getSelectedFile();
			int[] sels = videoTable.getSelectedRows();
			final List<AVExport> exportList = new ArrayList<>();
            for (int sel : sels) {
                VideoEntry ve = videoModel.rows.get(videoTable.convertRowIndexToModel(sel));

                AVExport ave = new AVExport();
                ave.video = rl.get(ve.path + "/" + ve.name, ResourceType.VIDEO);
                if (ve.audio != null && !ve.audio.isEmpty()) {
                    ave.audio = rl.getExactly(ve.audio, ve.path + "/" + ve.name, ResourceType.AUDIO);
                }
                ave.naming = lastDir.getAbsolutePath() + "/" + ve.name + "_%05d.png";
                exportList.add(ave);
            }
			doExportGUI(exportList);
		}
	}
	/**
	 * Construct an export indicator dialog.
	 * @param exportList the list to export
	 */
	void doExportGUI(final List<AVExport> exportList) {
		final JDialog exportDialog = new JDialog(this, true);
		exportDialog.setTitle("Export progress");
		final JLabel totalLabel = new JLabel("Total");
		final JLabel currentLabel = new JLabel("Current");
		final JProgressBar totalProgress = new JProgressBar(0, exportList.size());
		final JProgressBar currentProgress = new JProgressBar();
		final JButton cancel = new JButton("Cancel");
		
		Container c = exportDialog.getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));
		c.add(totalLabel);
		c.add(totalProgress);
		c.add(currentLabel);
		c.add(currentProgress);
		c.add(cancel);
		
		final Worker worker = new Worker() {
			@Override
			protected void work() {
				try {
					int count = 0;
					for (AVExport ave : exportList) {
						if (Thread.currentThread().isInterrupted()) {
							break;
						}
						final int j = ++count;
						
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								totalLabel.setText("Total: " + j + " of " + exportList.size());
								totalProgress.setValue(j - 1);
							}
						});
						exportVideo(ave.video, ave.audio, ave.naming, currentLabel, currentProgress);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								totalProgress.setValue(j);
							}
						});
					}
				} catch (Throwable t) {
					Exceptions.add(t);
				}
			}
			@Override
			protected void done() {
				exportDialog.dispose();
			}
		};
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				worker.interrupt();
			}
		});
		
		worker.start();

		exportDialog.pack();
		exportDialog.setSize(400, exportDialog.getHeight());
		exportDialog.setLocationRelativeTo(this);
		exportDialog.setVisible(true);
		exportDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				worker.interrupt();
			}
		});
	}
	/**
	 * Export the contents of the video into a sequence of PNG files.
	 * @param video the resource place to use
	 * @param audio the optional audio for balancing the frames and the audio length
	 * @param fileNameBase the formatter for the filename base.
	 * @param currentLabel the label to use for the current status display
	 * @param currentProgress the progress bar to use for the current frame value
	 */
	void exportVideo(ResourcePlace video, ResourcePlace audio, 
			final String fileNameBase, final JLabel currentLabel, final JProgressBar currentProgress) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				currentProgress.setValue(0);
				currentLabel.setText("Current file: " + fileNameBase);
			}
		});
		try (DataInputStream in = new DataInputStream(
				new BufferedInputStream(new GZIPInputStream(video.open(), 1024 * 1024), 1024 * 1024))) {
			int w = Integer.reverseBytes(in.readInt());
			int h = Integer.reverseBytes(in.readInt());
			final int frames = Integer.reverseBytes(in.readInt());
			double fps = Integer.reverseBytes(in.readInt()) / 1000.0;
			
			int[] palette = new int[256];
			byte[] bytebuffer = new byte[w * h];
			int[] currentImage = new int[w * h];
			int frameCount = 0;
			
			int frames2 = frames;
			if (audio != null) {
				try (AudioInputStream ain = AudioSystem.getAudioInputStream(new BufferedInputStream(
							audio.open(), 256 * 1024))) {
					frames2 = (int)Math.ceil(ain.available() * fps / 22050.0);
				} catch (UnsupportedAudioFileException ex) {
					Exceptions.add(ex);
				}
			}
			final int f = Math.max(frames, frames2);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					currentProgress.setMaximum(f);
					currentLabel.setText("Current file: " + fileNameBase + ", 0 of " + f);
				}
			});
			if (audio != null) {
				// make a copy of the audio
				String audioNaming = fileNameBase.substring(0, fileNameBase.length() - 9) + ".wav";
				try (InputStream asrc = audio.open();
					FileOutputStream acopy = new FileOutputStream(audioNaming)) {
					byte[] ab = new byte[1024 * 1024];
					while (!Thread.currentThread().isInterrupted()) {
						int read = asrc.read(ab);
						if (read < 0) {
							break;
						} else
						if (read > 0) {
							acopy.write(ab, 0, read);
						}
					}
				}
			}
			
			BufferedImage frameImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			while (!Thread.currentThread().isInterrupted()) {
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
					in.readFully(bytebuffer);
					for (int i = 0; i < bytebuffer.length; i++) {
						int c0 = palette[bytebuffer[i] & 0xFF];
						if (c0 != 0) {
							currentImage[i] = c0;
						}
					}
					frameImage.setRGB(0, 0, w, h, currentImage, 0, w);
	       			ImageIO.write(frameImage, "png", new File(String.format(fileNameBase, frameCount)));
	       			frameCount++;
	       			final int fc = frameCount;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentProgress.setValue(fc);
							currentLabel.setText("Current file: " + String.format(fileNameBase, fc) + ", " + fc + " of " + f);
						}
					});
				}
			}
			// continue to emit reposition events
			if (frames2 > frames && !Thread.currentThread().isInterrupted()) {
				for (int i = frames; i < frames2 && !Thread.currentThread().isInterrupted(); i++) {
	       			ImageIO.write(frameImage, "png", new File(String.format(fileNameBase, frameCount)));
	       			frameCount++;
	       			final int fc = frameCount;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentProgress.setValue(fc);
							currentLabel.setText("Current file: " + String.format(fileNameBase, fc) + ", " + fc + " of " + f);
						}
					});
				}
			}
		} catch (IOException ex) {
			Exceptions.add(ex);
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
		} catch (InvocationTargetException | InterruptedException e) {
			Exceptions.add(e);
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
		} else {
			subtitle.setText("");
		}
	}
}
