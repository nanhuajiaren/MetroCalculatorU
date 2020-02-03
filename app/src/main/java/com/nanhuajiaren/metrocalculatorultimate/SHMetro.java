package com.nanhuajiaren.metrocalculatorultimate;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.google.gson.Gson;
import android.content.Context;
import com.nanhuajiaren.metrocalculatorultimate.R;
import java.lang.reflect.Field;

public class SHMetro
{
	private Context ctx;
	
	@SerializedName("version")
	protected int dataVersion;
	
	@SerializedName("update_time")
	protected String updateTime;
	
	@SerializedName("update_info")
	protected String updateInfo;
	
	@SerializedName("global_ending_number_mode")
	protected List<EndNumberMode> globalEndingNumberMode;

	public int getDataVersion()
	{
		return dataVersion;
	}

	public String getUpdateTime()
	{
		return updateTime;
	}
	
	public String getUpdateInfo()
	{
		return updateInfo;
	}
	
	public class EndNumberMode
	{
		@SerializedName("train_length")
		protected int trainLength = 0;

		@SerializedName("mode")
		protected List<Integer> numberMode = null;

		public boolean verify(String carrageNumber,int locationInTrain){
			if(locationInTrain >= numberMode.size() || locationInTrain < 0){
				return false;
			}
			return carrageNumber.endsWith(numberMode.get(locationInTrain) + "");
		}
	}
	
	public boolean verifyGlobalEndingNumberMode(int trainLength,String carrageNumber,int locationInTrain){
		for(int i = 0;i < globalEndingNumberMode.size();i ++){
			if(globalEndingNumberMode.get(i).trainLength == trainLength){
				return globalEndingNumberMode.get(i).verify(carrageNumber,locationInTrain);
			}
		}
		return false;
	}
	
	protected List<MetroLine> lines;
	
	public class MetroLine
	{
		@SerializedName("line_name")
		protected String lineName;
		@SerializedName("number_header_new")
		protected String numberHeader;
		@SerializedName("is_metro")
		protected boolean isMetro = true;
		@SerializedName("train_length")
		protected int trainLength;
		
		protected List<Block> blocks;
		
		public class Block
		{
			public static final String TYPE_RAW = "raw";
			public static final String TYPE_OLD = "old";
			public static final String TYPE_MODERN = "modern";

			@SerializedName("type")
			protected String type;
			@SerializedName("block_height")
			protected int blockHeight = -1;
			@SerializedName("block_width")
			protected int blockWidth = -1;
			protected String year;
			@SerializedName("train_number_start")
			protected int trainNumberStart = -1;
			@SerializedName("m_number_start")
			protected int mNumberStart = -1;
			@SerializedName("ending_number_mode")
			protected List<Integer> endNumberMode;
			
			@SerializedName("train_data")
			protected List<TrainData> trainData;
			
			public class TrainData{
				@SerializedName("carrage_body_numbers")
				protected List<String> carrageBodyNumbers;
				@SerializedName("train_number")
				protected int trainNumber = -1;
			}
			
			public int getBlockHeight(){
				if(type.equals(TYPE_RAW)){
					return trainData.size();
				}else if(type.equals(TYPE_OLD) || type.equals(TYPE_MODERN)){
					return blockHeight;
				}else{
					return -1;
				}
			}
			
			public int calculate(String fullCarrageNumber){
				if(TYPE_RAW.equals(type)){
					for(int i = 0;i < trainData.size();i ++){
						TrainData data = trainData.get(i);
						for(int j = 0;j < data.carrageBodyNumbers.size();j ++){
							if(data.carrageBodyNumbers.get(j).equals(fullCarrageNumber)){
								return data.trainNumber;
							}
						}
					}
					return -1;
				}else if(TYPE_OLD.equals(type)){
					if(fullCarrageNumber.length() != 5 || !fullCarrageNumber.startsWith(year)){
						return -1;
					}else{
						int x = Integer.parseInt(fullCarrageNumber.substring(2,4));
						if(x > blockWidth * blockHeight){
							return -1;
						}
						if(!verifyEndNumber(fullCarrageNumber,(x - 1) % blockWidth)){
							return -1;
						}
						return (x - 1) / blockWidth + trainNumberStart;
					}
				}else if(TYPE_MODERN.equals(type)){
					if(fullCarrageNumber.length() != numberHeader.length() + 4 || !fullCarrageNumber.startsWith(numberHeader)){
						return -1;
					}else{
						int x = Integer.parseInt(fullCarrageNumber.substring(numberHeader.length(),numberHeader.length() + 3));
						if(x >= mNumberStart && x <= mNumberStart - 1 + blockWidth * blockHeight){
							if(!verifyEndNumber(fullCarrageNumber,(x - mNumberStart) % blockWidth)){
								return -1;
							}
							return (x - mNumberStart) / blockWidth + trainNumberStart;
						}else{
							return -1;
						}
					}
				}else{
					return -1;
				}
			}
			
			public boolean verifyEndNumber(String carrageNumber,int locationInTrain){
				if(endNumberMode == null){
					return verifyGlobalEndingNumberMode(blockWidth,carrageNumber,locationInTrain);
				}else{
					return carrageNumber.endsWith(endNumberMode.get(locationInTrain) + "");
				}
			}
		}
		
		public void initialize(){
			int lastTrainNumberEnd = 0;
			int lastMNumberEnd = 0;
			for(int i = 0;i < blocks.size();i ++){
				Block block = blocks.get(i);
				if(block.type == null){
					block.type = Block.TYPE_MODERN;
				}
				if(block.blockHeight <= 0 && block.type.equals(Block.TYPE_RAW)){
					block.blockHeight = block.trainData.size();
				}
				if(block.blockWidth <= 0){
					block.blockWidth = trainLength;
				}
				if(block.trainNumberStart < 1){
					block.trainNumberStart = lastTrainNumberEnd + 1;
				}
				lastTrainNumberEnd = block.trainNumberStart - 1 + block.blockHeight;
				if(block.mNumberStart < 1){
					block.mNumberStart = lastMNumberEnd + 1;
				}
				lastMNumberEnd = block.mNumberStart - 1 + block.blockHeight * block.blockWidth;
				try
				{
					Field f = Block.class.getDeclaredField("this$0");
					f.setAccessible(true);
					f.set(block,MetroLine.this);
				}
				catch (NoSuchFieldException e)
				{}
				catch (IllegalArgumentException e)
				{}
				catch (IllegalAccessException e)
				{}
				catch (SecurityException e)
				{}
			}
			for(int i = 0;i < trainTypes.size();i ++){
				TrainType t = trainTypes.get(i);
				if(t.outputNumberHeader == null){
					t.outputNumberHeader = numberHeader;
				}
				if(t.outputLineName == null){
					t.outputLineName = lineName;
				}
				try
				{
					Field fatherObjField = TrainType.class.getDeclaredField("this$0");
					fatherObjField.setAccessible(true);
					fatherObjField.set(t,MetroLine.this);
				}
				catch (NoSuchFieldException e)
				{}
				catch (IllegalArgumentException e)
				{}
				catch (IllegalAccessException e)
				{}
				catch (SecurityException e)
				{}
			}
		}
		
		@SerializedName("train_types")
		protected List<TrainType> trainTypes;
		
		public class TrainType{
			@SerializedName("train_type_code")
			protected String trainTypeCode;
			@SerializedName("train_type_name")
			protected String trainTypeName;
			@SerializedName("train_fill_mode")
			protected String trainFillMode = "";
			protected List<Integer> trains;
			@SerializedName("number_header_new")
			protected String outputNumberHeader = MetroLine.this.numberHeader;
			@SerializedName("line_name")
			protected String outputLineName = MetroLine.this.lineName;
			
			public boolean isThisType(int trainNumberInThisLine){
				if("raw".equals(trainFillMode)){
					for(int i = 0;i < trains.size();i ++){
						if(trains.get(i) == trainNumberInThisLine){
							return true;
						}
					}
					return false;
				}
				return trainNumberInThisLine >= trains.get(0) && trainNumberInThisLine <= trains.get(1);
			}
			
			public String formatNumber(int trainNumberInLine){
				StringBuilder sb = new StringBuilder();
				sb.append(ctx.getString(R.string.line));
				sb.append(outputLineName);
				sb.append("\n");
				sb.append(ctx.getString(R.string.train_type));
				sb.append(trainTypeCode + " " + trainTypeName);
				sb.append("\n");
				sb.append(ctx.getString(R.string.train_number));
				sb.append(outputNumberHeader + NumberFormat.showNumberInNoLessThan(trainNumberInLine,3));
				return sb.toString();
			}
		}
		
		public String calculate(String carrageNumber){
			for(int i = 0;i < blocks.size();i ++){
				int j = blocks.get(i).calculate(carrageNumber);
				if(j != -1){
					return formatNumber(j);
				}
			}
			return null;
		}
		
		public String formatNumber(int trainNumber){
			for(int i = 0;i < trainTypes.size();i ++){
				if(trainTypes.get(i).isThisType(trainNumber)){
					return trainTypes.get(i).formatNumber(trainNumber);
				}
			}
			return null;
		}
	}
	
	private SHMetro(){}
	
	public static SHMetro from(String json,Context ctx){
		SHMetro metro = new Gson().fromJson(json,SHMetro.class);
		metro.init();
		metro.ctx = ctx;
		return metro;
	}
	
	public void init(){
		for(int i = 0;i < lines.size();i ++){
			try
			{
				Field f = MetroLine.class.getDeclaredField("this$0");
				f.setAccessible(true);
				f.set(lines.get(i),SHMetro.this);
			}
			catch (NoSuchFieldException e)
			{}
			catch (IllegalArgumentException e)
			{}
			catch (IllegalAccessException e)
			{}
			catch (SecurityException e)
			{}
			lines.get(i).initialize();
		}
	}
	
	public String calculate(String carrageNumber){
		StringBuilder sb = new StringBuilder("");
		for(int i = 0;i < lines.size();i ++){
			String s = lines.get(i).calculate(carrageNumber);
			if(s != null){
				sb.append(s);
				sb.append("\n\n");
			}
		}
		return sb.toString();
	}
}
