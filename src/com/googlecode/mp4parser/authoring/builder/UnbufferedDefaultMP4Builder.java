package com.googlecode.mp4parser.authoring.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.DataEntryUrlBox;
import com.coremedia.iso.boxes.DataInformationBox;
import com.coremedia.iso.boxes.DataReferenceBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.SyncSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

/**
 * Creates a plain MP4 file from a video. Plain as plain can be.
 */
public class UnbufferedDefaultMP4Builder
{
   Set<StaticChunkOffsetBox> chunkOffsetBoxes = new HashSet<StaticChunkOffsetBox>();

   private final long movieTimeScale;

   private final DefaultMp4Builder builder = new DefaultMp4Builder();
   private final FileChannel output;

   private final Movie movie = new Movie();

   private final long headerOffset;
   private long dataOffset;

   private long chunkSize = 0;
   private Track currentTrack = null;

   //TODO: Get rid of ArrayList<Long>
   private HashMap<Track, ArrayList<Long>> chunkOffsets = new HashMap<Track, ArrayList<Long>>();
   private HashMap<Track, ArrayList<Long>> chunkSizes = new HashMap<Track, ArrayList<Long>>();
   private HashMap<Track, ArrayList<Long>> sampleSizes = new HashMap<Track, ArrayList<Long>>();

   private final FileOutputStream fileOutputStream;

   public UnbufferedDefaultMP4Builder(File file, long movieTimeScale) throws IOException
   {
      this.movieTimeScale = movieTimeScale;
      fileOutputStream = new FileOutputStream(file);
      output = fileOutputStream.getChannel();

      Box fileTypeBox = builder.createFileTypeBox(movie);
      fileTypeBox.getBox(output);

      headerOffset = fileTypeBox.getSize();
      dataOffset = headerOffset + 16;

      output.position(dataOffset);
   }

   private boolean isSmallBox(long contentSize)
   {
      return (contentSize + 8) < 4294967296L;
   }

   public void writeMetaData() throws IOException
   {
      chunkSizes.get(currentTrack).add(chunkSize);

      List<Track> tracks = movie.getTracks();
      for (Track track : tracks)
      {
         track.close();
      }

      writeMDatHeader();
      MovieBox movieBox = createMovieBox(movie);
      movieBox.getBox(output);
      fileOutputStream.close();
   }

   private void writeMDatHeader() throws IOException
   {
      long position = output.position();
      long size = position - headerOffset;
      ByteBuffer bb = ByteBuffer.allocate(16);
      if (isSmallBox(size))
      {
         IsoTypeWriter.writeUInt32(bb, size);
      }
      else
      {
         IsoTypeWriter.writeUInt32(bb, 1);
      }
      bb.put(IsoFile.fourCCtoBytes("mdat"));
      if (isSmallBox(size))
      {
         bb.put(new byte[8]);
      }
      else
      {
         IsoTypeWriter.writeUInt64(bb, size);
      }
      bb.rewind();
      output.position(headerOffset);
      output.write(bb);
      output.position(position);
   }

   protected void writeSample(Track track, ByteBuffer[] sample) throws IOException
   {
      if (track != currentTrack)
      {
         if (currentTrack != null)
         {
            chunkSizes.get(currentTrack).add(chunkSize);
         }
         chunkOffsets.get(track).add(output.position());
         currentTrack = track;
         chunkSize = 0;
      }
      int length = 0;
      for (int i = 0; i < sample.length; i++)
      {
         length += output.write(sample[i]);
      }
      sampleSizes.get(track).add((long) length);
      chunkSize += length;
   }

   public void forceChunck()
   {
      currentTrack = null;
   }

   protected MovieBox createMovieBox(Movie movie)
   {
      MovieBox movieBox = new MovieBox();
      MovieHeaderBox mvhd = new MovieHeaderBox();

      mvhd.setCreationTime(new Date());
      mvhd.setModificationTime(new Date());
      mvhd.setMatrix(movie.getMatrix());

      long duration = 0;

      for (Track track : movie.getTracks())
      {
         long tracksDuration = track.getDuration() * movieTimeScale / track.getTrackMetaData().getTimescale();
         if (tracksDuration > duration)
         {
            duration = tracksDuration;
         }

      }

      mvhd.setDuration(duration);
      mvhd.setTimescale(movieTimeScale);
      // find the next available trackId
      long nextTrackId = 0;
      for (Track track : movie.getTracks())
      {
         nextTrackId = nextTrackId < track.getTrackMetaData().getTrackId() ? track.getTrackMetaData().getTrackId() : nextTrackId;
      }
      mvhd.setNextTrackId(++nextTrackId);

      movieBox.addBox(mvhd);
      for (Track track : movie.getTracks())
      {
         movieBox.addBox(createTrackBox(track, movie));
      }
      return movieBox;

   }

   protected TrackBox createTrackBox(Track track, Movie movie)
   {

      TrackBox trackBox = new TrackBox();
      TrackHeaderBox tkhd = new TrackHeaderBox();

      tkhd.setEnabled(true);
      tkhd.setInMovie(true);
      tkhd.setInPreview(true);
      tkhd.setInPoster(true);
      tkhd.setMatrix(track.getTrackMetaData().getMatrix());

      tkhd.setAlternateGroup(track.getTrackMetaData().getGroup());
      tkhd.setCreationTime(track.getTrackMetaData().getCreationTime());
      // We need to take edit list box into account in trackheader duration
      // but as long as I don't support edit list boxes it is sufficient to
      // just translate media duration to movie timescale
      tkhd.setDuration(track.getDuration() * movieTimeScale / track.getTrackMetaData().getTimescale());
      tkhd.setHeight(track.getTrackMetaData().getHeight());
      tkhd.setWidth(track.getTrackMetaData().getWidth());
      tkhd.setLayer(track.getTrackMetaData().getLayer());
      tkhd.setModificationTime(new Date());
      tkhd.setTrackId(track.getTrackMetaData().getTrackId());
      tkhd.setVolume(track.getTrackMetaData().getVolume());

      trackBox.addBox(tkhd);

      /*
       * EditBox edit = new EditBox(); EditListBox editListBox = new
       * EditListBox(); editListBox.setEntries(Collections.singletonList( new
       * EditListBox.Entry(editListBox, (long)
       * (track.getTrackMetaData().getStartTime() * getTimescale(movie)), -1,
       * 1))); edit.addBox(editListBox); trackBox.addBox(edit);
       */

      MediaBox mdia = new MediaBox();
      trackBox.addBox(mdia);
      MediaHeaderBox mdhd = new MediaHeaderBox();
      mdhd.setCreationTime(track.getTrackMetaData().getCreationTime());
      mdhd.setDuration(track.getDuration());
      mdhd.setTimescale(track.getTrackMetaData().getTimescale());
      mdhd.setLanguage(track.getTrackMetaData().getLanguage());
      mdia.addBox(mdhd);
      HandlerBox hdlr = new HandlerBox();
      mdia.addBox(hdlr);

      hdlr.setHandlerType(track.getHandler());

      MediaInformationBox minf = new MediaInformationBox();
      minf.addBox(track.getMediaHeaderBox());

      // dinf: all these three boxes tell us is that the actual
      // data is in the current file and not somewhere external
      DataInformationBox dinf = new DataInformationBox();
      DataReferenceBox dref = new DataReferenceBox();
      dinf.addBox(dref);
      DataEntryUrlBox url = new DataEntryUrlBox();
      url.setFlags(1);
      dref.addBox(url);
      minf.addBox(dinf);
      //

      Box stbl = createStbl(track, movie);
      minf.addBox(stbl);
      mdia.addBox(minf);

      return trackBox;
   }

   protected Box createStbl(Track track, Movie movie)
   {
      SampleTableBox stbl = new SampleTableBox();

      createStsd(track, stbl);
      createStts(track, stbl);
      createCtts(track, stbl);
      createStss(track, stbl);
      createSdtp(track, stbl);
      createStsc(track, stbl);
      createStsz(track, stbl);
      createStco(track, movie, stbl);

      return stbl;
   }

   protected void createStsd(Track track, SampleTableBox stbl)
   {
      stbl.addBox(track.getSampleDescriptionBox());
   }

   protected void createStts(Track track, SampleTableBox stbl)
   {
      TimeToSampleBox.Entry lastEntry = null;
      List<TimeToSampleBox.Entry> entries = new ArrayList<TimeToSampleBox.Entry>();

      for (long delta : track.getSampleDurations())
      {
         if (lastEntry != null && lastEntry.getDelta() == delta)
         {
            lastEntry.setCount(lastEntry.getCount() + 1);
         }
         else
         {
            lastEntry = new TimeToSampleBox.Entry(1, delta);
            entries.add(lastEntry);
         }

      }
      TimeToSampleBox stts = new TimeToSampleBox();
      stts.setEntries(entries);
      stbl.addBox(stts);
   }

   protected void createCtts(Track track, SampleTableBox stbl)
   {
      List<CompositionTimeToSample.Entry> compositionTimeToSampleEntries = track.getCompositionTimeEntries();
      if (compositionTimeToSampleEntries != null && !compositionTimeToSampleEntries.isEmpty())
      {
         CompositionTimeToSample ctts = new CompositionTimeToSample();
         ctts.setEntries(compositionTimeToSampleEntries);
         stbl.addBox(ctts);
      }
   }

   protected void createStss(Track track, SampleTableBox stbl)
   {
      long[] syncSamples = track.getSyncSamples();
      if (syncSamples != null && syncSamples.length > 0)
      {
         SyncSampleBox stss = new SyncSampleBox();
         stss.setSampleNumber(syncSamples);
         stbl.addBox(stss);
      }
   }

   protected void createSdtp(Track track, SampleTableBox stbl)
   {
      if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty())
      {
         SampleDependencyTypeBox sdtp = new SampleDependencyTypeBox();
         sdtp.setEntries(track.getSampleDependencies());
         stbl.addBox(sdtp);
      }
   }

   protected void createStsc(Track track, SampleTableBox stbl)
   {

      SampleToChunkBox stsc = new SampleToChunkBox();
      stsc.setEntries(new LinkedList<SampleToChunkBox.Entry>());
      long lastChunkSize = Integer.MIN_VALUE; // to be sure the first chunks hasn't got the same size
      ArrayList<Long> tracksChunkSizes = chunkSizes.get(track);
      for (int i = 0; i < tracksChunkSizes.size(); i++)
      {
         // The sample description index references the sample description box
         // that describes the samples of this chunk. My Tracks cannot have more
         // than one sample description box. Therefore 1 is always right
         // the first chunk has the number '1'
         if (lastChunkSize != tracksChunkSizes.get(i))
         {
            stsc.getEntries().add(new SampleToChunkBox.Entry(i + 1, tracksChunkSizes.get(i), 1));
            lastChunkSize = tracksChunkSizes.get(i);
         }
      }
      stbl.addBox(stsc);
   }

   protected void createStsz(Track track, SampleTableBox stbl)
   {
      SampleSizeBox stsz = new SampleSizeBox();
      ArrayList<Long> arrayList = this.sampleSizes.get(track);
      long[] sampleSizes = new long[arrayList.size()];
      for (int i = 0; i < arrayList.size(); i++)
      {
         sampleSizes[i] = arrayList.get(i);
      }
      stsz.setSampleSizes(sampleSizes);

      stbl.addBox(stsz);
   }

   protected void createStco(Track track, Movie movie, SampleTableBox stbl)
   {

      // The ChunkOffsetBox we create here is just a stub
      // since we haven't created the whole structure we can't tell where the
      // first chunk starts (mdat box). So I just let the chunk offset
      // start at zero and I will add the mdat offset later.
      StaticChunkOffsetBox stco = new StaticChunkOffsetBox();
      this.chunkOffsetBoxes.add(stco);
      long[] chunkOffset = new long[chunkOffsets.size()];
      ArrayList<Long> arrayList = chunkOffsets.get(track);
      for (int i = 0; i < arrayList.size(); i++)
      {
         chunkOffset[i] = arrayList.get(i);
      }

      stco.setChunkOffsets(chunkOffset);
      stbl.addBox(stco);
   }

   public void addTrack(UnbufferedH264TrackImpl track)
   {
      movie.addTrack(track);

      chunkOffsets.put(track, new ArrayList<Long>());
      chunkSizes.put(track, new ArrayList<Long>());
      sampleSizes.put(track, new ArrayList<Long>());

   }
}
