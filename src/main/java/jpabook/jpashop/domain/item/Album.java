package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity

@DiscriminatorValue("A")// 싱글테이블 구분 하는 방법
@Getter @Setter
public class Album extends Item{
    private String artis;
    private String etc;
}
