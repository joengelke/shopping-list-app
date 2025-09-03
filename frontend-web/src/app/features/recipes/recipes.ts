import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatNavList } from '@angular/material/list';
import { RecipesService, Recipe } from '../../core/recipes/recipes';
import { MatInputModule } from '@angular/material/input';
import { UserService } from '../../core/user/user';

@Component({
  standalone: true,
  selector: 'app-recipes',
  templateUrl: './recipes.html',
  styleUrl: './recipes.scss',
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatListModule,
    MatIconModule,
    MatInputModule,
  ],
})
export class RecipesComponent implements OnInit {
  currentUserId: string | null = null;
  recipes: Recipe[] = [];
  selectedRecipe: Recipe | null = null;

  isCreatingRecipe = false;
  newRecipe: Recipe = {
    id: '', // leave empty, backend will generate
    name: '',
    creatorId: '', // leave empty, backend will set
    createdAt: '', // leave empty, backend will set
    itemSet: {
      id: '',
      name: '',
      itemList: [],
      receiptFileId: '',
    },
    description: '',
    instructions: [],
    categories: [],
    visibility: 'PRIVATE', // default value
    sharedWithUserIds: [],
    receiptFileId: '',
  };

  isEditMode: Boolean = false;
  editableRecipe: Recipe | null = null;

  constructor(
    private recipesService: RecipesService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.loadRecipes();
    this.currentUserId = this.userService.getCurrentUserId();
  }

  loadRecipes() {
    this.recipesService.getRecipes().subscribe({
      next: (recipes) => (this.recipes = recipes),
      error: (err) => (this.recipes = []),
    });
  }

  selectRecipe(recipe: Recipe) {
    this.selectedRecipe = recipe;
    this.isCreatingRecipe = false;
  }

  startCreate() {
    this.isCreatingRecipe = true;
    this.selectedRecipe = null;
    this.newRecipe = {
      id: '', // leave empty, backend will generate
      name: '',
      creatorId: '', // leave empty, backend will set
      createdAt: '', // leave empty, backend will set
      itemSet: {
        id: '',
        name: '',
        itemList: [],
        receiptFileId: '',
      },
      description: '',
      instructions: [],
      categories: [],
      visibility: 'PRIVATE', // default value
      sharedWithUserIds: [],
      receiptFileId: '',
    };
  }

  addCategory(recipe: Recipe) {
    recipe.categories.push('');
  }

  removeCategory(recipe: Recipe, index: number) {
    recipe.categories.splice(index, 1);
  }

  addIngredient(recipe: Recipe) {
    recipe.itemSet.itemList.push({
      id: '',
      tmpId: '',
      name: '',
      amount: 0,
      unit: '',
    });
  }

  removeIngredient(recipe: Recipe, index: number) {
    recipe.itemSet.itemList.splice(index, 1);
  }

  addInstruction(recipe: Recipe) {
    recipe.instructions.push('');
  }

  removeInstruction(recipe: Recipe, index: number) {
    recipe.instructions.splice(index, 1);
  }

  trackByIndex(index: number, item: any): number {
    return index;
  }

  saveRecipe(recipe: Recipe) {
    const cleanedRecipe: Recipe = {
      ...recipe,
      itemSet: {
        ...recipe.itemSet,
        name: recipe.name.trim(),
        itemList: recipe.itemSet.itemList
          .map((item) => ({ ...item, name: item.name.trim() }))
          .filter((item) => item.name && item.name.trim() !== ''),
      },
      instructions: recipe.instructions
        .map((instr) => instr.trim())
        .filter((instr) => instr !== ''),
      categories: recipe.categories
        .map((cat) => cat.trim())
        .filter((cat) => cat !== ''),
    };

    // Determine if it's a new recipe or an existing one
    if (!recipe.id) {
      // New recipe
      this.recipesService.createRecipe(cleanedRecipe).subscribe({
        next: (createdRecipe) => {
          this.recipes.push(createdRecipe);
          this.resetRecipe(recipe);
        },
        error: (err) => console.error('Error creating recipe', err),
      });
    } else {
      // Existing recipe (editableRecipe)
      this.recipesService.updateRecipe(cleanedRecipe).subscribe({
        next: (updatedRecipe) => {
          const index = this.recipes.findIndex(
            (r) => r.id === updatedRecipe.id
          );
          if (index > -1) this.recipes[index] = updatedRecipe;
          this.resetRecipe(recipe);
        },
        error: (err) => console.error('Error updating recipe', err),
      });
    }
  }

  resetRecipe(recipe: Recipe) {
    recipe.id = '';
    recipe.name = '';
    recipe.creatorId = '';
    recipe.createdAt = '';
    recipe.itemSet = { id: '', name: '', itemList: [], receiptFileId: '' };
    recipe.description = '';
    recipe.instructions = [];
    recipe.categories = [];
    recipe.visibility = 'PRIVATE';
    recipe.sharedWithUserIds = [];
    recipe.receiptFileId = '';
    this.isCreatingRecipe = false;
    this.selectedRecipe = null;
  }

  // EDIT section

  startEdit(recipe: Recipe) {
    if (recipe.creatorId === this.currentUserId) {
      this.isEditMode = true;

      this.editableRecipe = {
        id: recipe.id,
        name: recipe.name ?? '',
        creatorId: recipe.creatorId ?? '',
        createdAt: recipe.createdAt ?? '',
        description: recipe.description ?? '',
        visibility: recipe.visibility ?? 'PRIVATE',
        receiptFileId: recipe.receiptFileId ?? '',
        itemSet: {
          id: recipe.itemSet?.id ?? '',
          name: recipe.itemSet?.name ?? '',
          receiptFileId: recipe.itemSet?.receiptFileId ?? '',
          itemList: [...(recipe.itemSet?.itemList ?? [])],
        },
        instructions: [...(recipe.instructions ?? [])],
        categories: [...(recipe.categories ?? [])],
        sharedWithUserIds: [...(recipe.sharedWithUserIds ?? [])],
      };
    }
  }

  cancelEdit() {
    this.isEditMode = false;
    this.editableRecipe = null;
  }

  saveEdit() {
    if (!this.editableRecipe) return;

    // Clean empty instructions/items/categories before sending
    const cleanedRecipe = {
      ...this.editableRecipe,
      instructions: this.editableRecipe.instructions
        .map((i) => i.trim())
        .filter((i) => i),
      categories: this.editableRecipe.categories
        .map((c) => c.trim())
        .filter((c) => c),
      itemSet: {
        ...this.editableRecipe.itemSet,
        itemList: this.editableRecipe.itemSet.itemList
          .map((i) => ({ ...i, name: i.name.trim() }))
          .filter((i) => i.name),
      },
    };

    this.recipesService.updateRecipe(cleanedRecipe).subscribe({
      next: (updatedRecipe) => {
        // Update locally
        const index = this.recipes.findIndex((r) => r.id === updatedRecipe.id);
        if (index !== -1) this.recipes[index] = updatedRecipe;

        this.selectedRecipe = updatedRecipe;
        this.cancelEdit();
      },
      error: (err) => console.error('Error updating recipe', err),
    });
  }
}
