package com.mygdx.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.assets.ConstantsAssets;
import com.mygdx.assets.StaticAssets;
import com.mygdx.assets.UiComponentAssets;
import com.mygdx.enums.EventTypeEnum;
import com.mygdx.factory.ListenerFactory;
import com.mygdx.manager.AssetsManager;
import com.mygdx.manager.BattleManager;
import com.mygdx.manager.StorySectionManager;
import com.mygdx.model.battle.Skill;
import com.mygdx.screen.BattleScreen;
import com.uwsoft.editor.renderer.actor.CompositeItem;
import com.uwsoft.editor.renderer.actor.LabelItem;

public class SkillStage extends BaseOverlapStage {
	private final String TAG = "BattleStage";
	private final String DEFAULT_VISIBILTY = "Default";
	private final String PRESSED_VISIBILTY = "pressed";
	private final int SKILL_TAB_SIZE = 7;
	@Autowired
	private BattleManager battleManager;
	@Autowired
	private StorySectionManager storySectionManager;
	@Autowired
	private UiComponentAssets uiComponentAssets;
	@Autowired
	private AssetsManager assetsManager;
	@Autowired
	private ConstantsAssets constantsAssets;
	@Autowired
	private ListenerFactory listenerFactory;
	// @Autowired
	// private GridHitbox gridHitbox;
	private Map<String, Array<String>> sceneConstants;
	public final String SCENE_NAME = "skill_scene";
	private Camera cam;
	private Map<Integer, Skill> skillInfo;
	private List<CompositeItem> useButtonList;

	@Override
	public void act() {
		setLabel(sceneConstants);
	}

	public Stage makeStage() {
		sceneConstants = constantsAssets.getSceneConstants(SCENE_NAME);
		assetsManager.initScene(SCENE_NAME);
		initSceneLoader(assetsManager.rm);
		sceneLoader.loadScene(SCENE_NAME);
		addActor(sceneLoader.getRoot());

		setCamera();
		setSkillType();
		setBackground();

		setLabel(sceneConstants);
		setAllVoidUseButton(sceneConstants, listenerFactory);
		setHighlight(sceneConstants);
		addUseButtonListener();

		return this;
	}

	private void addUseButtonListener() {
		for (int i = 0; i < SKILL_TAB_SIZE; i++) {
			final int index = i;
			useButtonList.get(i).clearListeners();
			useButtonList.get(i).addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					setCompositeItemVisibilty(useButtonList.get(index), PRESSED_VISIBILTY);
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					setCompositeItemVisibilty(useButtonList.get(index), DEFAULT_VISIBILTY);
					battleManager.setCurrentSelectedSkill(skillInfo.get(index));
					storySectionManager.triggerNextSectionEvent(EventTypeEnum.BATTLE_CONTROL, "skill_attack");
					Skill currentSelectedSkill = battleManager.getCurrentSelectedSkill();
					if (currentSelectedSkill.getHitboxSize() == 0) {
						// gridHitbox.setHitboxCenter(currentSelectedSkill.getHitboxCenter());
						// gridHitbox.setHitboxShape(currentSelectedSkill.getHitboxShape());
						battleManager.afterClick(currentSelectedSkill.getCostGauge());
						battleManager.setShowGrid(true);
						Gdx.app.log(TAG, "gridHitbox를 표시합니다");
					} else {
						battleManager.setGridLimitNum(currentSelectedSkill.getHitboxSize());
						if (currentSelectedSkill.getHitboxCenter() == null) {
							battleManager.afterClick(currentSelectedSkill.getCostGauge());
							battleManager.setShowGrid(true);
						} else {
							Gdx.app.log(TAG, "스킬 즉시 사용");
							battleManager.afterClick(currentSelectedSkill.getCostGauge());
							battleManager.useSkill(battleManager.getCurrentAttackUnit(),
									battleManager.getSelectedMonster(), currentSelectedSkill.getName());
						}
					}
					BattleScreen.showSkillStage = false;
				}
			});
		}

	}

	private void setBackground() {
		final CompositeItem background = sceneLoader.getRoot().getCompositeById("background");
		background.setTouchable(Touchable.enabled);
		background.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				BattleScreen.showSkillStage = false;
				battleManager.showRMenuButtons();
			}
		});
	}

	private void setAllVoidUseButton(Map<String, Array<String>> sceneConstants, ListenerFactory listenerFactory) {
		Array<String> useButtonNames = sceneConstants.get("use_button");
		useButtonList = new ArrayList<>(SKILL_TAB_SIZE);
		for (int i = 0; i < SKILL_TAB_SIZE; i++) {
			CompositeItem useButton = sceneLoader.getRoot().getCompositeById(useButtonNames.get(i));
			useButton.setVisible(false);
			useButtonList.add(useButton);
		}
	}

	private void setVoidUseButton(final int index) {
		useButtonList.get(index).setVisible(false);
		useButtonList.get(index).setTouchable(Touchable.disabled);
	}

	private void setUseButton(final int index) {
		useButtonList.get(index).setVisible(true);
		setCompositeItemVisibilty(useButtonList.get(index), DEFAULT_VISIBILTY);
		useButtonList.get(index).setTouchable(Touchable.enabled);
	}

	private void setLabel(Map<String, Array<String>> sceneConstants) {
		int playerSkillSize = battleManager.getCurrentAttackUnit().getSkills().size();
		Array<String> skillNameLabelList = sceneConstants.get("skill_name_label");
		Array<String> castingLabelList = sceneConstants.get("casting_label");
		Array<String> gaugeLabelList = sceneConstants.get("gauge_label");
		skillInfo = new HashMap<>(SKILL_TAB_SIZE);
		for (int i = 0; i < SKILL_TAB_SIZE; i++) {
			LabelItem skillNameLabel = sceneLoader.getRoot().getLabelById(skillNameLabelList.get(i));
			LabelItem castingLabel = sceneLoader.getRoot().getLabelById(castingLabelList.get(i));
			LabelItem gaugeLabel = sceneLoader.getRoot().getLabelById(gaugeLabelList.get(i));
			if (playerSkillSize > i) {
				skillInfo.put(i, battleManager.getCurrentAttackUnit().getSkills().get(i));
				skillNameLabel.setText(battleManager.getCurrentAttackUnit().getSkills().get(i).getName());
				castingLabel.setText("");
				gaugeLabel.setText(
						String.valueOf(battleManager.getCurrentAttackUnit().getSkills().get(i).getCostGauge()));
				setLabelStyle(skillNameLabel);
				setLabelStyle(castingLabel);
				setLabelStyle(gaugeLabel);
			} else {
				skillNameLabel.setText("");
				castingLabel.setText("");
				gaugeLabel.setText("");
			}
		}
	}

	private void setLabelStyle(LabelItem labelItem) {
		labelItem.setStyle(new LabelStyle(uiComponentAssets.getFont(), Color.WHITE));
		labelItem.setFontScale(1.0f);
		labelItem.setTouchable(Touchable.disabled);
	}

	private void setSkillType() {
		// skillTypeButton = new ArrayList<CompositeItem>();
		final CompositeItem skillTypeButton_01 = sceneLoader.getRoot().getCompositeById("ability");
		final CompositeItem skillTypeButton_02 = sceneLoader.getRoot().getCompositeById("magic");

		skillTypeButton_01.setLayerVisibilty("Default", true);
		skillTypeButton_01.setLayerVisibilty("pressed", false);
		skillTypeButton_01.setTouchable(Touchable.enabled);
		skillTypeButton_01.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				skillTypeButton_01.setLayerVisibilty("pressed", true);
				skillTypeButton_01.setLayerVisibilty("Default", false);
				skillTypeButton_02.setLayerVisibilty("Default", true);
				skillTypeButton_02.setLayerVisibilty("pressed", false);
			}

		});
		// skillTypeButton.add(skillTypeButton_01);
		skillTypeButton_02.setLayerVisibilty("Default", true);
		skillTypeButton_02.setLayerVisibilty("pressed", false);
		skillTypeButton_02.setTouchable(Touchable.enabled);
		skillTypeButton_02.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				skillTypeButton_02.setLayerVisibilty("pressed", true);
				skillTypeButton_02.setLayerVisibilty("Default", false);
				skillTypeButton_01.setLayerVisibilty("Default", true);
				skillTypeButton_01.setLayerVisibilty("pressed", false);
			}

		});
		// skillTypeButton.add(skillTypeButton_02);
	}

	private void setCompositeItemVisibilty(CompositeItem compositeItem, String visibilty) {
		if (visibilty == PRESSED_VISIBILTY) {
			compositeItem.setLayerVisibilty(PRESSED_VISIBILTY, true);
			compositeItem.setLayerVisibilty(DEFAULT_VISIBILTY, false);
		} else {
			compositeItem.setLayerVisibilty(PRESSED_VISIBILTY, false);
			compositeItem.setLayerVisibilty(DEFAULT_VISIBILTY, true);
		}
	}

	private void setHighlight(final Map<String, Array<String>> sceneConstants) {
		final Array<String> highLightFrameList = sceneConstants.get("highlight_frame");
		for (int i = 0; i < SKILL_TAB_SIZE; i++) {
			final int focusedIndex = i;
			final CompositeItem highLightFrame = sceneLoader.getRoot().getCompositeById(highLightFrameList.get(i));
			setCompositeItemVisibilty(highLightFrame, DEFAULT_VISIBILTY);
			highLightFrame.setTouchable(Touchable.enabled);

			highLightFrame.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					for (int j = 0; j < SKILL_TAB_SIZE; j++) {
						if (skillInfo.get(j) != null) {
							final CompositeItem highLightFrame = sceneLoader.getRoot()
									.getCompositeById(highLightFrameList.get(j));
							if (j == focusedIndex) {
								highLightFrame.setLayerVisibilty(PRESSED_VISIBILTY, true);
								setUseButton(j);
							} else {
								highLightFrame.setLayerVisibilty(PRESSED_VISIBILTY, false);
								setVoidUseButton(j);
							}
						} else {
							final CompositeItem highLightFrame = sceneLoader.getRoot()
									.getCompositeById(highLightFrameList.get(j));

							highLightFrame.setLayerVisibilty(PRESSED_VISIBILTY, false);
							setVoidUseButton(j);
						}
					}

				}
			});
		}
	}

	private void setCamera() {
		cam = new OrthographicCamera(StaticAssets.BASE_WINDOW_WIDTH, StaticAssets.BASE_WINDOW_HEIGHT);
		cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
		getViewport().setCamera(cam);
	}
}
