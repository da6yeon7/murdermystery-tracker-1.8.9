package me.pompurin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Mod(modid = pompurinn.MODID, version = pompurinn.VERSION)
public class pompurinn
{
	EntityPlayer murderer, secondMurderer;
	boolean ingame, toggled, murdererIsSelf, doubles, murdererFound, secondMurdererFound = false;
	String name, secondName = "";
	ArrayList<Item> items = new ArrayList<Item>();
	KeyBinding solver;
	
	public static final String MODID = "murdermysterytracker";
    public static final String VERSION = "1.0.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        
        solver = new KeyBinding("Toggle mm tracker", Keyboard.KEY_M, "murder mystery tracker");
        ClientRegistry.registerKeyBinding(solver);
        
        items.add(new Item().getByNameOrId("minecraft:iron_sword"));
        items.add(new Item().getByNameOrId("minecraft:gold_sword"));
        items.add(new Item().getByNameOrId("minecraft:golden_carrot"));
        items.add(new Item().getByNameOrId("minecraft:wooden_axe"));
        items.add(new Item().getByNameOrId("minecraft:stone_sword"));
        items.add(new Item().getByNameOrId("minecraft:speckled_melon"));
        items.add(new Item().getByNameOrId("minecraft:boat"));
        items.add(new Item().getByNameOrId("minecraft:redstone_torch"));
        items.add(new Item().getByNameOrId("minecraft:fish"));
        items.add(new Item().getByNameOrId("minecraft:shears"));
        items.add(new Item().getByNameOrId("minecraft:diamond_hoe"));
        items.add(new Item().getByNameOrId("minecraft:diamond_sword"));
        items.add(new Item().getByNameOrId("minecraft:cooked_beef"));
        items.add(new Item().getByNameOrId("minecraft:prismarine_shard"));
        items.add(new Item().getByNameOrId("minecraft:double_plant"));
        items.add(new Item().getByNameOrId("minecraft:diamond_axe"));
        items.add(new Item().getByNameOrId("minecraft:cookie"));
        items.add(new Item().getByNameOrId("minecraft:carrot"));
        items.add(new Item().getByNameOrId("minecraft:bone"));
        items.add(new Item().getByNameOrId("minecraft:carrot_on_a_stick"));
        items.add(new Item().getByNameOrId("minecraft:sponge"));
        items.add(new Item().getByNameOrId("minecraft:name_tag"));
        items.add(new Item().getByNameOrId("minecraft:apple"));
        items.add(new Item().getByNameOrId("minecraft:golden_pickaxe"));
        items.add(new Item().getByNameOrId("minecraft:pumpkin_pie"));
        items.add(new Item().getByNameOrId("minecraft:diamond_shovel"));
        items.add(new Item().getByNameOrId("minecraft:feather"));
        items.add(new Item().getByNameOrId("minecraft:blaze_rod"));
        items.add(new Item().getByNameOrId("minecraft:stone_shovel"));
        items.add(new Item().getByNameOrId("minecraft:deadbush"));
        items.add(new Item().getByNameOrId("minecraft:wooden_sword"));
        items.add(new Item().getByNameOrId("minecraft:stick"));
        items.add(new Item().getByNameOrId("minecraft:iron_shovel"));
    }
    
    @SubscribeEvent
    public void onKey(KeyInputEvent event)
    {
    	if(solver.isPressed())
    	{
    		if(toggled)
        	{
        		murderer = null;
        		toggled = ingame = murdererIsSelf = doubles = murdererFound = secondMurdererFound = false;
        		name = secondName = "";
        	}
        	else
        	{
        		toggled = true;
        	}
        	Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "Murder Mystery Tracker has been set to " + EnumChatFormatting.AQUA + toggled));
    	}
    }
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event)
	{
		if(!toggled) return;
		String message = event.message.getUnformattedText();
		if(message.contains("The Murderer has received their sword!"))
		{
			murderer = null;
			ingame = true;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Murderer Tracking Started"));
		}
		
		else if(message.contains("The Murderers have received their swords!"))
		{
			murderer = null;
			secondMurderer = null;
			ingame = true;
			doubles = true;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Murderer Tracking Started - Doubles Mode"));
		}
		
		else if(message.contains("You have received your sword!"))
		{
			murderer = null;
			ingame = true;
			murdererIsSelf = true;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You are the Murderer! Good luck!"));
		}
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase != Phase.START || toggled == false) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.theWorld;
		EntityPlayerSP player = mc.thePlayer;
		
		if(world != null && ingame)
		{
			List<String> lines = new ArrayList<>();
			Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
			if (scoreboard == null) ingame = false;
			else
			{
				ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
				if (objective == null) ingame = false;
				else
				{
					Collection<Score> scores = scoreboard.getSortedScores(objective);
					List<Score> list = scores.stream()
							.filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
							.startsWith("#"))
							.collect(Collectors.toList());
					
					if (list.size() > 15) {
						scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
					} else {
						scores = list;
					}
					
					for (Score score : scores) {
						ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
						lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
					}
				}
			}
			
			ingame = false;
			for(String line : lines)
			{
				if(line.toLowerCase().contains("innocents left"))
					ingame = true;
			}
			
			if(ingame == false)
			{
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Murderer Tracking Stopped"));
				murderer = secondMurderer = null;
				murdererFound = secondMurdererFound = doubles = murdererIsSelf = false;
				name = secondName = "";
			}
			
			if(player != null && !murdererFound && !murdererIsSelf)
			{
		    	List<EntityPlayer> players = world.playerEntities;
		    	
		    	if(players.size() > 0)
		    	{
		    		for(EntityPlayer otherPlayer : players)
		    		{
		    			if(!otherPlayer.isInvisible())
		    			{
		    				ItemStack itemstack = otherPlayer.getHeldItem();
			    			if(itemstack != null && items.contains(itemstack.getItem()))
			    			{
			    				if(!doubles)
			    				{
			    					murderer = otherPlayer;
			    					name = otherPlayer.getName();
			    					murdererFound = true;
			    					player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The Murderer is " + murderer.getName() + "! Holding item: " + itemstack.getDisplayName() + ". Registry Name: " + itemstack.getItem().getRegistryName()));
			    				}
			    				else if(!secondMurdererFound)
			    				{
			    					secondMurderer = otherPlayer;
			    					secondName = otherPlayer.getName();
			    					secondMurdererFound = true;
			    					player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The First Murderer is " + secondMurderer.getName() + "! Holding item: " + itemstack.getDisplayName() + ". Registry Name: " + itemstack.getItem().getRegistryName()));
			    				}
			    				else if(!otherPlayer.getName().equals(secondName))
			    				{
			    					murderer = otherPlayer;
			    					name = otherPlayer.getName();
			    					murdererFound = true;
			    					player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The Second Murderer is " + murderer.getName() + "! Holding item: " + itemstack.getDisplayName() + ". Registry Name: " + itemstack.getItem().getRegistryName()));
			    				}
			    			}
		    			}	    			
		    		}
		    	}
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldRender(RenderWorldLastEvent event)
	{
		if(toggled && ingame && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null)
		{
			Minecraft mc = Minecraft.getMinecraft();
			World world = mc.theWorld;
			EntityPlayerSP player = mc.thePlayer;
			
			if(murdererIsSelf)
			{
				List<EntityPlayer> players = world.playerEntities;
		    	for(EntityPlayer otherPlayer : players)
		    	{
		    		if(!(otherPlayer.isInvisible() || otherPlayer.isPlayerSleeping() || otherPlayer.getName().equals(player.getName())))
		    		{
		    			draw3DLine(player.getPositionEyes(player.getEyeHeight()), otherPlayer.getPositionEyes(otherPlayer.getEyeHeight()), new Color(255, Math.min(255, (int)(player.getDistanceToEntity(otherPlayer)*5)), 0), event.partialTicks);
		    		}
		    	}
			}
			
			AxisAlignedBB aabb = new AxisAlignedBB(player.posX - 100, player.posY - 50, player.posZ - 100, player.posX + 100, player.posY + 50, player.posZ + 100);
			EntityItem goldIngot = (EntityItem)world.findNearestEntityWithinAABB(EntityItem.class, aabb, player);
			if(goldIngot != null && goldIngot.getEntityItem().getItem().getRegistryName().contains("gold_ingot"))
			{
				draw3DLine(player.getPositionEyes(player.getEyeHeight()), goldIngot.getPositionVector(), new Color(0, 255, 0), event.partialTicks);
			}
			
			if(doubles && secondMurdererFound)
			{
				List<EntityPlayer> players = world.playerEntities;
				secondMurderer = world.getPlayerEntityByName(secondName);
				if(secondMurderer != null && !(secondMurderer.isInvisible() || secondMurderer.isPlayerSleeping() || !players.contains(secondMurderer)))
					draw3DLine(player.getPositionEyes(player.getEyeHeight()), secondMurderer.getPositionEyes(secondMurderer.getEyeHeight()), new Color(255, Math.min(255, (int)(player.getDistanceToEntity(secondMurderer)*5)), 0), event.partialTicks);
			}
			
			if(murdererFound)
			{
				List<EntityPlayer> players = world.playerEntities;
				murderer = world.getPlayerEntityByName(name);
				if(murderer != null && !(murderer.isInvisible() || murderer.isPlayerSleeping() || !players.contains(murderer)))
					draw3DLine(player.getPositionEyes(player.getEyeHeight()), murderer.getPositionEyes(murderer.getEyeHeight()), new Color(255, Math.min(255, (int)(player.getDistanceToEntity(murderer)*5)), 0), event.partialTicks);
			}
		}
	}
	
	public void draw3DLine(Vec3 pos1, Vec3 pos2, Color colour, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.theWorld;
		EntityPlayerSP player = mc.thePlayer;
		
		Entity render = Minecraft.getMinecraft().getRenderViewEntity();
		WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
		
		double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
		double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
		double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;
		
		GlStateManager.pushMatrix();
		//GlStateManager.translate(-realX, -realY, -realZ);
		GlStateManager.translate(-player.posX, -player.posY, -player.posZ); //tester code
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GL11.glLineWidth(2);
		GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
		worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		
		
		Vec3 vec = player.getPositionVector(); //start: tester code
		double mx = vec.xCoord;
		double mz = vec.zCoord;
		double my = vec.yCoord + player.getEyeHeight();
		double drawBeforeCameraDist = 1.0; //distance from camera to tracer start
		double pitch = ((player.rotationPitch + 90) * Math.PI) / 180;
		double yaw = ((player.rotationYaw + 90) * Math.PI) / 180;
		mx += Math.sin(pitch) * Math.cos(yaw) * drawBeforeCameraDist;
		mz += Math.sin(pitch) * Math.sin(yaw) * drawBeforeCameraDist;
		my += Math.cos(pitch) * drawBeforeCameraDist; //end: tester code

		
		worldRenderer.pos(mx, my, mz).endVertex();
		//worldRenderer.pos(pos1.xCoord, pos1.yCoord, pos1.zCoord).endVertex();
		worldRenderer.pos(pos2.xCoord, pos2.yCoord, pos2.zCoord).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.translate(realX, realY, realZ);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}
}
