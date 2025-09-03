import { TestBed } from '@angular/core/testing';

import { ShoppingListService } from './shopping-list';

describe('ShoppingList', () => {
  let service: ShoppingListService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShoppingListService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
