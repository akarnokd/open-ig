package hu.openig.music;

import java.io.*;
import javax.sound.midi.*;

public class PlayerPiano {
    // These are some MIDI constants from the spec.  They aren't defined
    // for us in javax.sound.midi.
    public static final int DAMPER_PEDAL = 64;
    public static final int DAMPER_ON = 127;
    public static final int DAMPER_OFF = 0;
    public static final int END_OF_TRACK = 47;

    public static void main(String[  ] args)
        throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        int instrument = 0;
        int tempo = 120;
        String filename = null;

        // Parse the options
        // -i <instrument number> default 0, a piano.  Allowed values: 0-127
        // -t <beats per minute>  default tempo is 120 quarter notes per minute
        // -o <filename>          save to a midi file instead of playing
        int a = 0; 
        while(a < args.length) {
            if (args[a].equals("-i")) {
                instrument = Integer.parseInt(args[a+1]);
                a+=2;
            }
            else if (args[a].equals("-t")) {
                tempo = Integer.parseInt(args[a+1]);
                a+=2;
            }
            else if (args[a].equals("-o")) {
                filename = args[a+1];
                a += 2;
            }
            else break;
        }

        char[  ] notes = args[a].toCharArray( );

        // 16 ticks per quarter note. 
        Sequence sequence = new Sequence(Sequence.PPQ, 16);

        // Add the specified notes to the track
        addTrack(sequence, instrument, tempo, notes);

        if (filename == null) {  // no filename, so play the notes
            // Set up the Sequencer and Synthesizer objects
            Sequencer sequencer = MidiSystem.getSequencer( );
            sequencer.open( );  
            Synthesizer synthesizer = MidiSystem.getSynthesizer( );
            synthesizer.open( );
            sequencer.getTransmitter( ).setReceiver(synthesizer.getReceiver( ));

            // Specify the sequence to play, and the tempo to play it at
            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM(tempo);

            // Let us know when it is done playing
            sequencer.addMetaEventListener(new MetaEventListener( ) {
                    public void meta(MetaMessage m) {
                        // A message of this type is automatically sent
                        // when we reach the end of the track
                        if (m.getType( ) == END_OF_TRACK) System.exit(0);
                    }
                });
            // And start playing now.
            sequencer.start( );
        }
        else {  // A file name was specified, so save the notes
            int[  ] allowedTypes = MidiSystem.getMidiFileTypes(sequence);
            if (allowedTypes.length == 0) {
                System.err.println("No supported MIDI file types.");
            }
            else {
                MidiSystem.write(sequence, allowedTypes[0],
                                 new File(filename));
                System.exit(0);
            }
        }
    }

    static final int[  ] offsets = {  // add these amounts to the base value
        // A   B  C  D  E  F  G
          -4, -2, 0, 1, 3, 5, 7  
    };

    /*
     * This method parses the specified char[  ] of notes into a Track.
     * The musical notation is the following:
     * A-G:   A named note; Add b for flat and # for sharp.
     * +:     Move up one octave. Persists.
     * -:     Move down one octave.  Persists.
     * /1:    Notes are whole notes.  Persists 'till changed
     * /2:    Half notes
     * /4:    Quarter notes
     * /n:    N can also be 8, 16, 32, 64.
     * s:     Toggle sustain pedal on or off (initially off)
     * 
     * >:     Louder.  Persists
     * <:     Softer.  Persists
     * .:     Rest. Length depends on current length setting
     * Space: Play the previous note or notes; notes not separated by spaces
     *        are played at the same time
     */
    public static void addTrack(Sequence s, int instrument, int tempo,
                                char[  ] notes)
        throws InvalidMidiDataException
    {
        Track track = s.createTrack( );  // Begin with a new track

        // Set the instrument on channel 0
        ShortMessage sm = new ShortMessage( );
        sm.setMessage(ShortMessage.PROGRAM_CHANGE, 0, instrument, 0);
        track.add(new MidiEvent(sm, 0));

        int n = 0; // current character in notes[  ] array
        int t = 0; // time in ticks for the composition

        // These values persist and apply to all notes 'till changed
        int notelength = 16; // default to quarter notes
        int velocity = 64;   // default to middle volume
        int basekey = 60;    // 60 is middle C. Adjusted up and down by octave
        boolean sustain = false;   // is the sustain pedal depressed?
        int numnotes = 0;    // How many notes in current chord?

        while(n < notes.length) {
            char c = notes[n++];

            if (c == '+') basekey += 12;        // increase octave
            else if (c == '-') basekey -= 12;   // decrease octave
            else if (c == '>') velocity += 16;  // increase volume;
            else if (c == '<') velocity -= 16;  // decrease volume;
            else if (c == '/') {
                char d = notes[n++];
                if (d == '2') notelength = 32;  // half note
                else if (d == '4') notelength = 16;  // quarter note
                else if (d == '8') notelength = 8;   // eighth note
                else if (d == '3' && notes[n++] == '2') notelength = 2;
                else if (d == '6' && notes[n++] == '4') notelength = 1;
                else if (d == '1') {
                    if (n < notes.length && notes[n] == '6')
                        notelength = 4;    // 1/16th note
                    else notelength = 64;  // whole note
                }
            }
            else if (c == 's') {
                sustain = !sustain;
                // Change the sustain setting for channel 0
                ShortMessage m = new ShortMessage( );
                m.setMessage(ShortMessage.CONTROL_CHANGE, 0,
                             DAMPER_PEDAL, sustain?DAMPER_ON:DAMPER_OFF);
                track.add(new MidiEvent(m, t));
            }
            else if (c >= 'A' && c <= 'G') {
                int key = basekey + offsets[c - 'A'];
                if (n < notes.length) {
                    if (notes[n] == 'b') { // flat
                        key--; 
                        n++;
                    }
                    else if (notes[n] == '#') { // sharp
                        key++;
                        n++;
                    }
                }

                addNote(track, t, notelength, key, velocity);
                numnotes++;
            }
            else if (c == ' ') {
                // Spaces separate groups of notes played at the same time.
                // But we ignore them unless they follow a note or notes.
                if (numnotes > 0) {
                    t += notelength;
                    numnotes = 0;
                }
            }
            else if (c == '.') { 
                // Rests are like spaces in that they force any previous
                // note to be output (since they are never part of chords)
                if (numnotes > 0) {
                    t += notelength;
                    numnotes = 0;
                }
                // Now add additional rest time
                t += notelength;
            }
        }
    }
        
    // A convenience method to add a note to the track on channel 0
    public static void addNote(Track track, int startTick,
                               int tickLength, int key, int velocity)
        throws InvalidMidiDataException
    {
        ShortMessage on = new ShortMessage( );
        on.setMessage(ShortMessage.NOTE_ON,  0, key, velocity);
        ShortMessage off = new ShortMessage( );
        off.setMessage(ShortMessage.NOTE_OFF, 0, key, velocity);
        track.add(new MidiEvent(on, startTick));
        track.add(new MidiEvent(off, startTick + tickLength));
    }
}