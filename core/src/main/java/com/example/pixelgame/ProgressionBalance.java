package com.example.pixelgame;

/** Fixed regional tuning from authored rewards, never from the player's live gear or wallet. */
final class ProgressionBalance {
    static final int ATTACK_PRICE=50, HEALTH_PRICE=40, HEAL_PRICE=20, BUFF_PRICE=35;
    static final int CLEAR_RATE_PERCENT=90;
    record Reference(int attackLevels,int healthLevels,int remainingGold){
        int damage(){return GameBalance.BASE_ATTACK_DAMAGE+attackLevels;}
        int health(){return GameBalance.BASE_HEALTH+healthLevels*GameBalance.HEALTH_UPGRADE;}
    }
    static int reward(Enemy.EnemyType type){
        return switch(type){case SLIME->10;case KNIGHT->20;case DARK_SLIME->30;
            case GLASSWING->GameBalance.MOTH_GOLD;case LANTERN_MARKSMAN->GameBalance.MARKSMAN_GOLD;};
    }
    static int ordinaryGold(int stage){return StageLayout.enemies(stage).stream().mapToInt(s->reward(s.type())).sum();}
    static int optionalGold(int stage){
        SideBossArena side=SideBossArena.forStage(stage);
        return side!=null?side.gold():stage==2?Lamplighter.GOLD:0;
    }
    static int totalGold(int stage){return ordinaryGold(stage)+optionalGold(stage);}
    static int budget(int stage){return totalGold(stage)*CLEAR_RATE_PERCENT/100;}
    private static final Reference[] REFERENCES={calculate(1),calculate(2),calculate(3),calculate(4)};
    static Reference reference(int stage){return REFERENCES[Math.max(1,Math.min(4,stage))-1];}
    /** Spend near-equal amounts on attack/health, carrying only unspendable change between regions. */
    private static Reference calculate(int stage){
        int attack=0,health=0,wallet=0;
        for(int region=1;region<Math.min(4,Math.max(1,stage));region++){
            wallet+=budget(region);
            while(wallet>=Math.min(ATTACK_PRICE,HEALTH_PRICE)){
                boolean preferAttack=attack*ATTACK_PRICE<=health*HEALTH_PRICE;
                if(wallet>=ATTACK_PRICE&&(preferAttack||wallet<HEALTH_PRICE)){attack++;wallet-=ATTACK_PRICE;}
                else if(wallet>=HEALTH_PRICE){health++;wallet-=HEALTH_PRICE;}
                else break;
            }
        }
        return new Reference(attack,health,wallet);
    }
    static int enemyHealth(int stage,int base){
        int region=Math.max(1,Math.min(4,stage));
        float growth=(float)reference(region).damage()/GameBalance.BASE_ATTACK_DAMAGE;
        return Math.max(1,Math.round(base*growth*(1+.08f*(region-1))));
    }
    static int enemyDamage(int stage,int base){
        int region=Math.max(1,Math.min(4,stage));
        float growth=(float)reference(region).health()/GameBalance.BASE_HEALTH;
        return Math.max(1,Math.round(base*growth*(1+.04f*(region-1))));
    }
    private ProgressionBalance(){}
}
