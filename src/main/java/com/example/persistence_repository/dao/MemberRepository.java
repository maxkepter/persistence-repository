package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Member;
import com.example.persistence_repository.persistence.repository.AbstractReposistory;
import com.example.persistence_repository.persistence.repository.CrudReposistory;
import com.example.persistence_repository.persistence.repository.RepositoryRegistry;

public class MemberRepository extends AbstractReposistory<Member, Integer> {

    public MemberRepository() {
        super(Member.class);
        RepositoryRegistry.register(Member.class, this);
    }

    @Override
    protected <R> CrudReposistory<R, Object> resolveRepository(Class<R> targetType) {
        return RepositoryRegistry.get(targetType);
    }
}
